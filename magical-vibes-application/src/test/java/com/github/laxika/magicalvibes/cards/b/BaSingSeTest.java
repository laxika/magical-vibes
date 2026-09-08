package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaSingSe.class, Forest.class})
class BaSingSeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playBaSingSe();

        assertThat(findPermanent(player1, "Ba Sing Se").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());
        playBaSingSe();

        assertThat(findPermanent(player1, "Ba Sing Se").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one green mana")
    void tappingProducesGreenMana() {
        addBaSingSeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sorcery-speed ability earthbends a land twice")
    void earthbendsTargetLand() {
        Permanent source = addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addBaSingSeReady(Player player) {
        Permanent perm = new Permanent(new BaSingSe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void playBaSingSe() {
        harness.setHand(player1, List.of(new BaSingSe()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
