package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KasminasTransmutation;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SparkDouble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UginTheIneffable.class, GrizzlyBears.class, MindStone.class, SparkDouble.class,
        KasminasTransmutation.class})
class UginTheIneffableTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless spells you cast cost {2} less")
    void reducesColorlessSpells() {
        addReadyUgin();
        harness.setHand(player1, List.of(new MindStone()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    @DisplayName("+1 exiles the top card face down and creates a 2/2 colorless Spirit")
    void plusOneExilesAndCreatesSpirit() {
        Permanent ugin = addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(gd.findExiledCard(exiledCard.getId()).faceDown()).isTrue();
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("When the Spirit leaves, its linked exiled card returns to hand")
    void spiritLeavesAndReturnsLinkedCard() {
        addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    @DisplayName("The Spirit trigger returns its card even after Ugin leaves")
    void spiritTriggerSurvivesUginLeaving() {
        Permanent ugin = addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ugin));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("−3 destroys a colored permanent")
    void minusThreeDestroysColoredPermanent() {
        addReadyUgin();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("−3 cannot target a colorless permanent")
    void minusThreeRejectsColorlessPermanent() {
        addReadyUgin();
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("colored permanent");
    }

    @Test
    void doesNotReduceColoredSpells() {
        addReadyUgin();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        addReadyUgin();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MindStone()));

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Mind Stone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void costReductionEndsWhenUginLeaves() {
        Permanent ugin = addReadyUgin();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ugin));
        harness.setHand(player1, List.of(new MindStone()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mind Stone");
    }

    @Test
    void emptyLibraryStillCreatesSpirit() {
        Permanent ugin = addReadyUgin();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spirit")).isNotNull();
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void bouncingSpiritReturnsLinkedCard() {
        addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, spirit));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void controllerCanLookAtExiledCardAfterUginLeaves() throws Exception {
        Permanent ugin = addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ugin));
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(controllerState.lookedAtExileCards()).extracting(card -> card.id())
                .contains(exiledCard.getId());
        assertThat(opponentState.lookedAtExileCards()).extracting(card -> card.id())
                .doesNotContain(exiledCard.getId());
    }

    @Test
    void copyOfSpiritDoesNotReturnOriginalsExiledCard() {
        addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        SparkDouble sparkDouble = new SparkDouble();
        harness.castFromHand(player1, sparkDouble, "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, spirit.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(sparkDouble.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, copy));
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void spiritLosingAbilitiesDoesNotPreventReturningLinkedCard() {
        addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        harness.setHand(player1, List.of(new KasminasTransmutation()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, spirit.getId());
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void returningFaceDownCardDoesNotRevealItsIdentityInPublicLog() {
        addReadyUgin();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gameLogContains("Grizzly Bears")).isFalse();
    }

    private Permanent addReadyUgin() {
        Permanent ugin = harness.addToBattlefieldAndReturn(player1, new UginTheIneffable());
        ugin.setCounterCount(CounterType.LOYALTY, 4);
        ugin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ugin;
    }
}
