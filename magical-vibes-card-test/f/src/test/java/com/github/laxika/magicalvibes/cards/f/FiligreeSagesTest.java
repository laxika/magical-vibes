package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiligreeSages.class, AngelsFeather.class, GrizzlyBears.class})
class FiligreeSagesTest extends BaseCardTest {

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        harness.addToBattlefield(player1, new FiligreeSages());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        target.tap();
        addAbilityMana();

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped artifact")
    void canUntapOwnArtifact() {
        harness.addToBattlefield(player1, new FiligreeSages());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        UUID targetId = target.getId();
        target.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can untap itself as an artifact creature")
    void canUntapSelf() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        sages.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, sages.getId());
        harness.passBothPriorities();

        assertThat(sages.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an untapped artifact and does not tap its source")
    void canTargetUntappedArtifact() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FiligreeSages());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(sages.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FiligreeSages());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FiligreeSages());
        sages.tap();
        first.tap();
        second.tap();
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(sages.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FiligreeSages());
        target.tap();
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(sages);
        gd.playerGraveyards.get(player1.getId()).add(sages.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not untap an artifact that leaves and returns before resolution")
    void doesNotUntapReturnedTarget() {
        harness.addToBattlefield(player1, new FiligreeSages());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FiligreeSages());
        target.tap();
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        returned.tap();
        harness.passBothPriorities();

        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot substitute generic mana for the required blue mana")
    void cannotActivateWithOnlyGenericMana() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sages.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
