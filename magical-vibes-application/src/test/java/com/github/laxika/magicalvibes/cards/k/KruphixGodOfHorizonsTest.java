package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KruphixGodOfHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Kruphix is an enchantment below seven devotion to green and blue")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(4);

        assertThat(gqs.isCreature(gd, kruphix)).isFalse();
        assertThat(gqs.isEnchantment(gd, kruphix)).isTrue();
    }

    @Test
    @DisplayName("Kruphix becomes a creature at seven devotion to green and blue")
    void becomesCreatureAtDevotionThreshold() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(5);

        assertThat(gqs.isCreature(gd, kruphix)).isTrue();
    }

    @Test
    @DisplayName("The controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        addKruphix();
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("The controller's mana becomes colorless instead of draining")
    void controllersManaBecomesColorlessInsteadOfDraining() {
        addKruphix();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addKruphix() {
        return harness.addToBattlefieldAndReturn(player1, new KruphixGodOfHorizons());
    }

    private void addGreenPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }
    }
}
