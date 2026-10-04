package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.t.TelJiladChosen;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlissaSunseeker.class, IronMyr.class, TelJiladChosen.class, WeldingJar.class})
class GlissaSunseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact whose mana value equals unspent mana")
    void destroysArtifactWithMatchingManaValue() {
        Permanent glissa = addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(glissa.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Does nothing when the artifact mana value does not match unspent mana")
    void doesNothingWithMismatchedManaValue() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Checks unspent mana when the ability resolves")
    void checksManaAtResolution() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerManaPools.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Uses the ability controller's unspent mana")
    void usesAbilityControllersUnspentMana() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Destroys a zero-mana-value artifact when no mana is unspent")
    void destroysZeroManaValueArtifactWithNoUnspentMana() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeldingJar());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Welding Jar");
    }

    @Test
    @DisplayName("Does nothing if the targeted artifact leaves before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeldingJar());
        Permanent regenerationTarget = harness.addToBattlefieldAndReturn(player2, new IronMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, 0, null, regenerationTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Welding Jar");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(regenerationTarget);
    }

    @Test
    @DisplayName("Can target an artifact even when its mana value currently does not match")
    void targetRestrictionOnlyRequiresArtifact() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addCreatureReady(player2, new TelJiladChosen());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Can gain the matching mana by activating a mana ability in response")
    void gainsMatchingManaInResponse() {
        addCreatureReady(player1, new GlissaSunseeker());
        addCreatureReady(player1, new IronMyr());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Iron Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not destroy an artifact when unspent mana exceeds its mana value")
    void doesNothingWithExcessMana() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the ability controller")
    void destroysOwnArtifact() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Iron Myr");
    }

    @Test
    @DisplayName("A targeted artifact can regenerate from Glissa's destruction")
    void targetCanRegenerate() {
        addCreatureReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addToBattlefield(player2, new WeldingJar());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Welding Jar");
        harness.assertOnBattlefield(player2, "Iron Myr");
        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new IronMyr());
    }
}
