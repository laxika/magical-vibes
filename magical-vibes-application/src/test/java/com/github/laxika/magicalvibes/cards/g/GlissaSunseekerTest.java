package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlissaSunseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact whose mana value equals unspent mana")
    void destroysArtifactWithMatchingManaValue() {
        addReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Does nothing when the artifact mana value does not match unspent mana")
    void doesNothingWithMismatchedManaValue() {
        addReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Checks unspent mana when the ability resolves")
    void checksManaAtResolution() {
        addReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerManaPools.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Can target an artifact even when its mana value currently does not match")
    void targetRestrictionOnlyRequiresArtifact() {
        addReady(player1, new GlissaSunseeker());
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        addReady(player1, new GlissaSunseeker());
        Permanent target = addReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private Permanent addArtifact(Player player) {
        harness.addToBattlefield(player, new Millstone());
        return findPermanent(player, "Millstone");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
