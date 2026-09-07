package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CursedRack;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({StrengthOfLunacy.class, CursedRack.class, GrizzlyBears.class, RavensCrime.class})
class StrengthOfLunacyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and protection from white")
    void enchantedCreatureGetsBoostAndProtection() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfLunacy());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Removing Strength of Lunacy removes its bonuses")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfLunacy());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Resolving Strength of Lunacy attaches it to a creature")
    void resolvesAndAttaches() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrengthOfLunacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == StrengthOfLunacy.class
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.setHand(player1, List.of(new StrengthOfLunacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Cursed Rack");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Madness casts Strength of Lunacy for black mana")
    void madnessCastsForBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        StrengthOfLunacy lunacy = new StrengthOfLunacy();
        harness.setHand(player1, List.of(lunacy));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lunacy.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
    }
}
