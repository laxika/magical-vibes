package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.SeizeTheStorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoppetStitcher.class, PoppetFactory.class, Opt.class, GrizzlyBears.class, SeizeTheStorm.class})
class PoppetStitcherTest extends BaseCardTest {

    @Test
    void instantOrSorceryCreatesADecayedZombie() {
        harness.addToBattlefield(player1, new PoppetStitcher());
        harness.setHand(player1, java.util.List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    void transformsAtUpkeepWithThreeCreatureTokens() {
        Permanent stitcher = harness.addToBattlefieldAndReturn(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        addCreatureToken(player1);

        advanceToPoppetUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(stitcher.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformWithOnlyTwoCreatureTokensYouControl() {
        Permanent stitcher = harness.addToBattlefieldAndReturn(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        addCreatureToken(player2);

        advanceToPoppetUpkeep(player1);

        assertThat(stitcher.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void factoryMakesOwnCreatureTokensThreeThreeAndRemovesTheirAbilities() {
        PoppetStitcher card = new PoppetStitcher();
        Permanent factory = new Permanent(card);
        factory.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(factory);

        harness.setHand(player1, java.util.List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        factory.setCard(card.getBackFaceCard());
        factory.setTransformed(true);

        Permanent zombie = findPermanent(player1, "Zombie");
        Permanent nontokenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCreatureToken(player2);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, zombie).losesAllAbilities()).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DECAYED)).isFalse();
        assertThat(bls.canBlock(gd, zombie)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nontokenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nontokenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    void factoryMayTransformBackAtUpkeep() {
        PoppetStitcher card = new PoppetStitcher();
        Permanent factory = new Permanent(card);
        factory.setSummoningSick(false);
        factory.setCard(card.getBackFaceCard());
        factory.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(factory);

        advanceToPoppetUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(factory.isTransformed()).isFalse();
    }

    @Test
    void sorceryCreatesZombieBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new PoppetStitcher());
        harness.setHand(player1, java.util.List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DECAYED)).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentInstantDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new PoppetStitcher());
        harness.setHand(player2, java.util.List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void creatureSpellDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new PoppetStitcher());
        harness.setHand(player1, java.util.List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineStitcherTransformation() {
        Permanent stitcher = harness.addToBattlefieldAndReturn(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        addCreatureToken(player1);

        advanceToPoppetUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(stitcher.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenThresholdIsCheckedAgainOnResolution() {
        Permanent stitcher = harness.addToBattlefieldAndReturn(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        Permanent thirdToken = addCreatureToken(player1);

        advanceToPoppetUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(thirdToken);
        harness.passBothPriorities();

        assertThat(stitcher.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stitcherDoesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        addCreatureToken(player1);

        advanceToPoppetUpkeep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void factoryMayRemainTransformedWithoutCreatureTokens() {
        PoppetStitcher card = new PoppetStitcher();
        Permanent factory = harness.addToBattlefieldAndReturn(player1, card);
        factory.setCard(card.getBackFaceCard());
        factory.setTransformed(true);

        advanceToPoppetUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(factory.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformingBackRestoresZombieAbilitiesAndBaseStats() {
        PoppetStitcher card = new PoppetStitcher();
        Permanent factory = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, java.util.List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent zombie = findPermanent(player1, "Zombie");
        factory.setCard(card.getBackFaceCard());
        factory.setTransformed(true);

        advanceToPoppetUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(factory.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DECAYED)).isTrue();
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    void factoryDoesNotCreateZombieWhenControllerCastsInstant() {
        PoppetStitcher card = new PoppetStitcher();
        Permanent factory = harness.addToBattlefieldAndReturn(player1, card);
        factory.setCard(card.getBackFaceCard());
        factory.setTransformed(true);
        harness.setHand(player1, java.util.List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(factory);
    }

    @Test
    void nontokenCreaturesDoNotSatisfyTokenThreshold() {
        Permanent stitcher = harness.addToBattlefieldAndReturn(player1, new PoppetStitcher());
        addCreatureToken(player1);
        addCreatureToken(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToPoppetUpkeep(player1);

        assertThat(stitcher.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        return harness.addToBattlefieldAndReturn(player, token);
    }

    private void advanceToPoppetUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}
