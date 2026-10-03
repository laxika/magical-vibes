package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicAppetite.class, GlorySeeker.class})
class DemonicAppetiteTest extends BaseCardTest {

    @Test
    void enchantsOnlyYourCreatureAndBoostsIt() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DemonicAppetite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void cannotEnchantAnOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DemonicAppetite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesAControllerCreatureAtYourUpkeep() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DemonicAppetite());
        aura.setAttachedTo(enchanted.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(5);
    }

    @Test
    void canChooseTheEnchantedCreatureToSacrifice() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DemonicAppetite());
        aura.setAttachedTo(enchanted.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchanted.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchanted.getCard(), aura.getCard());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void sacrificesTheEnchantedCreatureWhenItIsTheOnlyCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DemonicAppetite());
        aura.setAttachedTo(enchanted.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchanted.getCard(), aura.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerAtOpponentsUpkeep() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DemonicAppetite());
        aura.setAttachedTo(enchanted.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(enchanted, aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
