package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.InnocuousRat;
import com.github.laxika.magicalvibes.cards.m.MidnightMayhem;
import com.github.laxika.magicalvibes.cards.m.MirrorRoomFracturedRealm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TobyBeastieBefriender.class, InnocuousRat.class, MidnightMayhem.class, MirrorRoomFracturedRealm.class})
class TobyBeastieBefrienderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 4/4 white Beast token with its restriction")
    void etbCreatesRestrictedBeastToken() {
        Permanent toby = castToby();
        Permanent beast = findPermanent(player1, "Beast");
        beast.setSummoningSick(false);
        toby.setSummoningSick(false);

        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(
                        gd.playerBattlefields.get(player1.getId()).indexOf(beast))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Four creature tokens give all creature tokens you control flying")
    void fourCreatureTokensGiveTokensFlying() {
        addTokenCreature("First Token");
        addTokenCreature("Second Token");
        addTokenCreature("Third Token");
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());

        castToby();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue());
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Fewer than four creature tokens do not grant flying")
    void fewerThanFourCreatureTokensDoNotGiveFlying() {
        addTokenCreature("First Token");
        addTokenCreature("Second Token");

        castToby();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .noneSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue());
    }

    @Test
    @DisplayName("The Beast can attack alongside Toby")
    void beastCanAttackWithAnotherCreature() {
        Permanent toby = castToby();
        Permanent beast = findPermanent(player1, "Beast");
        toby.setSummoningSick(false);
        beast.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(toby),
                gd.playerBattlefields.get(player1.getId()).indexOf(beast)));

        assertThat(toby.isAttacking()).isTrue();
        assertThat(beast.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The Beast cannot block alone even when Toby is available to block")
    void beastCannotBlockAlone() {
        castToby();
        Permanent beast = findPermanent(player1, "Beast");
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        attacker.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(beast), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block alone");
    }

    @Test
    @DisplayName("The Beast can block with another creature blocking a different attacker")
    void beastCanBlockWhenAnotherCreatureAlsoBlocks() {
        Permanent toby = castToby();
        Permanent beast = findPermanent(player1, "Beast");
        Permanent first = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(
                        new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(beast), 0),
                        new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(toby), 1))));

        assertThat(beast.isBlocking()).isTrue();
        assertThat(toby.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying updates when creature tokens enter or leave and ends when Toby leaves")
    void flyingTracksTokenCountAndSourcePresence() {
        Permanent toby = castToby();
        Permanent beast = findPermanent(player1, "Beast");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new MidnightMayhem()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gremlin")).hasSize(3);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanents(player1, "Gremlin").getFirst());
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();

        addTokenCreature("Replacement Token");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
        addTokenCreature("Fifth Token");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, toby);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Noncreature tokens and opposing creature tokens do not count toward flying")
    void onlyOwnCreatureTokensCountTowardFlying() {
        addTokenCreature("First Token");
        addTokenCreature("Second Token");
        castToby();
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setToken(true);
        Permanent artifactToken = harness.addToBattlefieldAndReturn(player1, treasure);
        Card opposingToken = new Card();
        opposingToken.setName("Opposing Token");
        opposingToken.setType(CardType.CREATURE);
        opposingToken.setPower(1);
        opposingToken.setToughness(1);
        opposingToken.setToken(true);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, opposingToken);
        Permanent beast = findPermanent(player1, "Beast");

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();
        addTokenCreature("Fourth Creature Token");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactToken, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Toby, Beastie Befriender"), Keyword.FLYING))
                .isFalse();
    }

    @Test
    @DisplayName("Toby's entry trigger still creates the Beast after Toby leaves")
    void entryTriggerResolvesAfterTobyLeaves() {
        harness.setHand(player1, List.of(new TobyBeastieBefriender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent toby = findPermanent(player1, "Toby, Beastie Befriender");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, toby);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Toby, Beastie Befriender");
    }

    @Test
    @DisplayName("A token copy of Toby grants itself flying with four creature tokens")
    void tokenCopyOfTobyReceivesItsOwnFlyingGrant() {
        addTokenCreature("First Token");
        Permanent original = castToby();
        harness.setHand(player1, List.of(new MirrorRoomFracturedRealm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, original.getId());
        resolveAllTriggers();

        Permanent tokenToby = findPermanents(player1, "Toby, Beastie Befriender").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, tokenToby.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toby, Beastie Befriender")).containsExactly(tokenToby);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(4);
        assertThat(gqs.hasKeyword(gd, tokenToby, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Beast"))
                .allSatisfy(beast -> assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue());
    }

    private Permanent castToby() {
        harness.setHand(player1, List.of(new TobyBeastieBefriender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Toby, Beastie Befriender");
    }

    private void addTokenCreature(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, token);
        permanent.setSummoningSick(false);
    }
}
