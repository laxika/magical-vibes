package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OrochiEggwatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrochiEggwatcher.class, ShidakoBroodmistress.class, SakuraTribeElder.class})
class ShidakoBroodmistressTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature to give a target creature +3/+3")
    void boostsTargetCreature() {
        addTransformedEggwatcher(player1);
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());
        var opponentSakura = harness.getPermanentId(player2, "Sakura-Tribe Elder");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentSakura);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sakura-Tribe Elder"));
        harness.passBothPriorities();

        Permanent boosted = findPermanent(player2, "Sakura-Tribe Elder");
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        addTransformedEggwatcher(player1);
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());
        var opponentSakura = harness.getPermanentId(player2, "Sakura-Tribe Elder");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentSakura);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sakura-Tribe Elder"));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent boosted = findPermanent(player2, "Sakura-Tribe Elder");
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can sacrifice itself as the creature cost")
    void canSacrificeItself() {
        Permanent shidako = addTransformedEggwatcher(player1);
        Permanent target = addCreatureReady(player1, new SakuraTribeElder());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, shidako.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shidako, Broodmistress");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without the {G}")
    void requiresGreenMana() {
        addTransformedEggwatcher(player1);
        addCreatureReady(player1, new SakuraTribeElder());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player1, "Sakura-Tribe Elder")))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedEggwatcher(Player player) {
        OrochiEggwatcher card = new OrochiEggwatcher();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }
}
