package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReadyToRumble.class, AirElemental.class, ChandraNalaar.class, FountainOfYouth.class, Goldhound.class})
class ReadyToRumbleTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(0, creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void dealsFiveDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);

        cast(0, planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(1, artifact);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    void damageModeCannotTargetAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactModeCannotTargetACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeCannotTargetAPlayer() {
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeCanTargetAnArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Goldhound());

        cast(0, creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Goldhound");
    }

    @Test
    void artifactModeCanDestroyYourOwnArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Goldhound());

        cast(1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Goldhound");
    }

    @Test
    void damageModeDoesNotResolveWhenTargetIsSacrificedInResponse() {
        Permanent creature = addCreatureReady(player2, new Goldhound());
        prepareCast();
        harness.castInstant(player1, 0, 0, creature.getId());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.RED.name());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Goldhound");
        harness.assertInGraveyard(player1, "Ready to Rumble");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int modeIndex, Permanent target) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, modeIndex, target.getId());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ReadyToRumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
