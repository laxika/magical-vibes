package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraugrNecromancer.class, Doomskar.class, Forest.class, Frogify.class, GrizzlyBears.class, Shock.class})
class DraugrNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nontoken creature with an ice counter instead of letting it die")
    void exilesOpponentNontokenCreatureWithIceCounter() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());

        assertThat(gd.findExiledCard(bearId)).isNotNull();
        assertThat(gd.exiledCardsWithIceCounters).contains(bearId);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(
                gd.findExiledCard(bearId).card());
    }

    @Test
    @DisplayName("Does not exile a token with an ice counter")
    void doesNotExileTokenWithIceCounter() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        Card token = createTokenCreature();
        UUID tokenId = token.getId();
        Permanent tokenPermanent = addCreatureReady(player2, token);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, tokenPermanent.getId());

        assertThat(gd.findExiledCard(tokenId)).isNull();
        assertThat(gd.exiledCardsWithIceCounters).doesNotContain(tokenId);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Casts an opponent-owned ice-counter card with snow mana as any color")
    void castsIceCounterCardWithSnowManaAsAnyColor() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, bearId))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, bearId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant permission to play an ice-counter land")
    void doesNotGrantPermissionForIceCounterLand() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        Forest forest = new Forest();
        gd.addToExileWithIceCounter(player2.getId(), forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing all abilities stops the exile replacement")
    void abilityLossStopsExileReplacement() {
        Permanent necromancer = harness.addToBattlefieldAndReturn(player1, new DraugrNecromancer());
        frogify(necromancer);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent permanent = addCreatureReady(player2, bear);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, permanent.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bear.getId())).isNull();
        assertThat(gd.exiledCardsWithIceCounters).doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Losing all abilities stops permission to cast ice-counter cards")
    void abilityLossStopsCastingPermission() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());
        Permanent necromancer = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Draugr Necromancer"));
        frogify(necromancer);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bearId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bearId)).isNotNull();
    }

    @Test
    @DisplayName("Does not exile a creature controlled by its controller")
    void doesNotExileOwnControlledCreature() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        GrizzlyBears bear = new GrizzlyBears();
        Permanent permanent = addCreatureReady(player1, bear);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bear.getId())).isNull();
    }

    @Test
    @DisplayName("Cannot cast an ice-counter card its controller owns")
    void cannotCastOwnIceCounterCard() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        GrizzlyBears bear = new GrizzlyBears();
        gd.addToExileWithIceCounter(player1.getId(), bear);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast an opponent-owned exiled card without an ice counter")
    void cannotCastCardWithoutIceCounter() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        GrizzlyBears bear = new GrizzlyBears();
        harness.setExile(player2, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A new Necromancer can cast cards exiled by an earlier Necromancer")
    void newNecromancerCanCastPreviouslyExiledCard() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bearId))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new DraugrNecromancer());
        harness.castFromExile(player1, bearId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bearId)).isNull();
    }

    @Test
    @DisplayName("Replacement still applies when Necromancer and opposing creatures die together")
    void exilesOpponentCreatureDuringSimultaneousDestruction() {
        DraugrNecromancer necromancer = new DraugrNecromancer();
        harness.addToBattlefield(player1, necromancer);
        GrizzlyBears bear = new GrizzlyBears();
        harness.addToBattlefield(player2, bear);
        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Draugr Necromancer");
        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        assertThat(gd.exiledCardsWithIceCounters).contains(bear.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles an opponent-controlled creature its controller owns but cannot cast it")
    void exilesOwnCardControlledByOpponentWithoutCastingPermission() {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        GrizzlyBears bear = new GrizzlyBears();
        bear.setOwnerId(player1.getId());
        Permanent permanent = addCreatureReady(player2, bear);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, permanent.getId());

        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        assertThat(gd.exiledCardsWithIceCounters).contains(bear.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ice-counter permission does not bypass creature spell timing")
    void cannotCastCreatureOnOpponentTurn() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bearId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bearId)).isNotNull();
    }

    private void frogify(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private UUID destroyOpponentCreature(Card creature) {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        Permanent permanent = addCreatureReady(player2, creature);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, permanent.getId());
        return creature.getId();
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
