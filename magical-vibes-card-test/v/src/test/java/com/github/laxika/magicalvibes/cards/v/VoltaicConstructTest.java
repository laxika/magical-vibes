package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.cards.t.TelJiladWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoltaicConstruct.class, DrossGolem.class, TelJiladWolf.class, DarksteelIngot.class})
class VoltaicConstructTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target artifact creature")
    void untapsTargetArtifactCreature() {
        Permanent construct = addCreatureReady(player1, new VoltaicConstruct());
        Permanent target = addCreatureReady(player2, new DrossGolem());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(construct.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        addCreatureReady(player1, new VoltaicConstruct());
        Permanent wolf = addCreatureReady(player2, new TelJiladWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        addCreatureReady(player1, new VoltaicConstruct());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can untap itself while tapped and summoning sick")
    void canUntapItselfWhileTappedAndSummoningSick() {
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new VoltaicConstruct());
        construct.setSummoningSick(true);
        construct.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, construct.getId());

        assertThat(construct.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(construct.isTapped()).isFalse();
        assertThat(construct.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Can target an already untapped artifact creature")
    void canTargetUntappedArtifactCreature() {
        addCreatureReady(player1, new VoltaicConstruct());
        Permanent target = addCreatureReady(player1, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only one mana")
    void cannotActivateWithInsufficientMana() {
        addCreatureReady(player1, new VoltaicConstruct());
        Permanent target = addCreatureReady(player2, new DrossGolem());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability still untaps its target after the source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent construct = addCreatureReady(player1, new VoltaicConstruct());
        Permanent target = addCreatureReady(player2, new DrossGolem());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(construct);
        gd.playerGraveyards.get(player1.getId()).add(construct.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability does not untap another creature when its target leaves the battlefield")
    void doesNotUntapAnotherCreatureWhenTargetLeaves() {
        Permanent construct = addCreatureReady(player1, new VoltaicConstruct());
        Permanent target = addCreatureReady(player2, new DrossGolem());
        construct.tap();
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(construct.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
