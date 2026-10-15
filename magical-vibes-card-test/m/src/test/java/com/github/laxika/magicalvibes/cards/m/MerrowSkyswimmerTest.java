package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreatForestDruid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerrowSkyswimmer.class, GreatForestDruid.class})
class MerrowSkyswimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white and blue Merfolk token")
    void createsMerfolkToken() {
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Merfolk");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Convoke taps creatures to help cast Merrow Skyswimmer")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreatureTappingPermanents(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MerrowSkyswimmer);
        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke both generic and hybrid costs without mana")
    void convokesEntireCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent whiteBlueFirst = harness.addToBattlefieldAndReturn(player1, new MerrowSkyswimmer());
        Permanent whiteBlueSecond = harness.addToBattlefieldAndReturn(player1, new MerrowSkyswimmer());
        List<Permanent> helpers = List.of(first, second, third, whiteBlueFirst, whiteBlueSecond);
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));

        harness.castCreatureTappingPermanents(player1, 0,
                helpers.stream().map(Permanent::getId).toList());

        assertThat(helpers).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Merrow Skyswimmer")).hasSize(3);
        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Green creatures and colorless mana cannot pay white-blue hybrid symbols")
    void convokeCannotUseGreenForHybridSymbols() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Merrow Skyswimmer")).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates the token for the entering creature's controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new MerrowSkyswimmer());

        assertThat(findPermanents(player2, "Merfolk")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Merfolk")).hasSize(1);
        assertThat(findPermanents(player1, "Merfolk")).isEmpty();
        assertThat(findPermanent(player2, "Merfolk").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The entry trigger still creates a token after its source leaves the battlefield")
    void entryTriggerSurvivesSourceLeaving() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new MerrowSkyswimmer());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
        assertThat(findPermanents(player1, "Merrow Skyswimmer")).isEmpty();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking, and vigilance leaves the attacker untapped")
    void flyingAndVigilanceApplyDuringCombat() {
        Permanent attacker = addCreatureReady(player1, new MerrowSkyswimmer());
        Permanent blocker = addCreatureReady(player2, new GreatForestDruid());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Another flying creature can block Merrow Skyswimmer")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new MerrowSkyswimmer());
        Permanent blocker = addCreatureReady(player2, new MerrowSkyswimmer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
