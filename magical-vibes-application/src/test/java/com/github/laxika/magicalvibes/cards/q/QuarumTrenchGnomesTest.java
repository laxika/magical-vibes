package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuarumTrenchGnomes.class, Plains.class, Forest.class, LightningBolt.class})
class QuarumTrenchGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the targeted Plains produce colorless mana")
    void makesTargetedPlainsProduceColorlessMana() {
        Permanent gnomes = addCreatureReady(player1, new QuarumTrenchGnomes());
        Permanent targetedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent otherPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, battlefieldIndex(gnomes), null, targetedPlains.getId());
        harness.passBothPriorities();

        harness.tapPermanent(player1, battlefieldIndex(targetedPlains));
        harness.tapPermanent(player1, battlefieldIndex(otherPlains));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Plains land")
    void cannotTargetNonPlainsLand() {
        Permanent gnomes = addCreatureReady(player1, new QuarumTrenchGnomes());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(gnomes), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The replacement remains after the targeted Plains untaps")
    void replacementRemainsAfterTargetedPlainsUntaps() {
        Permanent gnomes = addCreatureReady(player1, new QuarumTrenchGnomes());
        Permanent targetedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, battlefieldIndex(gnomes), null, targetedPlains.getId());
        harness.passBothPriorities();

        harness.tapPermanent(player1, battlefieldIndex(targetedPlains));
        harness.performUntapStep(player1);
        harness.tapPermanent(player1, battlefieldIndex(targetedPlains));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("The replacement remains after Quarum Trench Gnomes leaves the battlefield")
    void replacementRemainsAfterGnomesLeavesBattlefield() {
        Permanent gnomes = addCreatureReady(player1, new QuarumTrenchGnomes());
        Permanent targetedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, battlefieldIndex(gnomes), null, targetedPlains.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, gnomes.getId());
        harness.tapPermanent(player1, battlefieldIndex(targetedPlains));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target a Plains controlled by an opponent")
    void canTargetOpponentsPlains() {
        Permanent gnomes = addCreatureReady(player1, new QuarumTrenchGnomes());
        Permanent targetedPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, battlefieldIndex(gnomes), null, targetedPlains.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player2, battlefieldIndex(player2, targetedPlains));

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    private int battlefieldIndex(Permanent permanent) {
        return battlefieldIndex(player1, permanent);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
