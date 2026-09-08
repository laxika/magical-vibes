package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfCourage;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeoninIconoclastTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic destroys an enchantment creature an opponent controls")
    void heroicDestroysEnchantmentCreatureOpponentControls() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, new ArchetypeOfCourage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player1, 0, iconoclastId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, enchantmentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantmentCreature);
    }

    @Test
    @DisplayName("Heroic cannot target a non-enchantment creature")
    void heroicCannotTargetNonEnchantmentCreature() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, new ArchetypeOfCourage());
        Permanent nonEnchantmentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player1, 0, iconoclastId);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonEnchantmentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, enchantmentCreature.getId());
    }

    @Test
    @DisplayName("An opponent's spell targeting Leonin Iconoclast does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player2, 0, iconoclastId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
