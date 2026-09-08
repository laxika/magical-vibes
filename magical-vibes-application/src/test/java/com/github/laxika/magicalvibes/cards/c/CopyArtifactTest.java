package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.Tranquility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopyArtifact.class, JayemdaeTome.class, GrizzlyBears.class, Juggernaut.class, Shatter.class,
        Tranquility.class})
class CopyArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an artifact and its activated ability works")
    void copiesArtifactAndItsActivatedAbilityWorks() {
        CopyArtifact source = new CopyArtifact();
        Permanent copy = copyFrom(source, new JayemdaeTome());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not offer to copy a non-artifact")
    void doesNotOfferToCopyNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        CopyArtifact source = new CopyArtifact();
        castCopyArtifact(source);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findCopy(source)).isNotNull();
    }

    @Test
    @DisplayName("Declining to copy leaves the enchantment unchanged")
    void decliningToCopyLeavesTheEnchantmentUnchanged() {
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        CopyArtifact source = new CopyArtifact();
        castCopyArtifact(source);
        harness.handleMayAbilityChosen(player1, false);
        Permanent entered = findCopy(source);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(entered), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        Tranquility tranquility = new Tranquility();
        harness.castFromHand(player1, tranquility, "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(entered);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tome);
    }

    @Test
    @DisplayName("A copied artifact can be destroyed by an artifact spell")
    void copiedArtifactCanBeDestroyedByArtifactSpell() {
        CopyArtifact source = new CopyArtifact();
        Permanent copy = copyFrom(source, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, copy.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("A copied artifact is also destroyed by an enchantment spell")
    void copiedArtifactIsAlsoDestroyedByEnchantmentSpell() {
        CopyArtifact source = new CopyArtifact();
        Permanent copy = copyFrom(source, new JayemdaeTome());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("Copies artifact creature characteristics and abilities")
    void copiesArtifactCreatureCharacteristicsAndAbilities() {
        CopyArtifact source = new CopyArtifact();
        Permanent copy = copyFrom(source, new Juggernaut());
        copy.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void castCopyArtifact(CopyArtifact copy) {
        harness.castFromHand(player1, copy, "{1}{U}");
        harness.passBothPriorities();
    }

    private Permanent copyFrom(CopyArtifact source, Card targetCard) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        castCopyArtifact(source);
        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        return findCopy(source);
    }

    private Permanent findCopy(CopyArtifact source) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(source.getId()))
                .findFirst()
                .orElseThrow();
    }
}
