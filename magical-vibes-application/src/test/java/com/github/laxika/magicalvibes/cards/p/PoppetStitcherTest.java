package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
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

@CardUsed({PoppetStitcher.class, PoppetFactory.class, Opt.class, GrizzlyBears.class})
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

    private Permanent addCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        return harness.addToBattlefieldAndReturn(player, token);
    }

    private void advanceToPoppetUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
