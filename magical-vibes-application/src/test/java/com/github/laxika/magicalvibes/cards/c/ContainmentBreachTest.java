package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DetentionVortex;
import com.github.laxika.magicalvibes.cards.m.MageHuntersOnslaught;
import com.github.laxika.magicalvibes.cards.z.ZephyrBoots;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContainmentBreach.class, DarksteelCitadel.class, GrizzlyBears.class,
        Millstone.class, RodOfRuin.class, DetentionVortex.class, ZephyrBoots.class,
        MageHuntersOnslaught.class})
class ContainmentBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a low-mana-value artifact and creates a Pest")
    void destroysLowManaValueArtifactAndCreatesPest() {
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");
        castContainmentBreach(targetId);

        assertNotOnBattlefield(targetId);
        assertThat(createdTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Pest for an artifact with mana value greater than 2")
    void doesNotCreatePestForHighManaValueArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        castContainmentBreach(targetId);

        assertNotOnBattlefield(targetId);
        assertThat(createdTokens()).isEmpty();
    }

    @Test
    @DisplayName("Creates a Pest even when a low-mana-value target is indestructible")
    void createsPestWhenTargetIsIndestructible() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Citadel");
        castContainmentBreach(targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId))
                .contains(targetId);
        assertThat(createdTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareContainmentBreach();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Destroys an enchantment and creates a Pest for its controller")
    void destroysLowManaValueEnchantment() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new ZephyrBoots());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DetentionVortex());
        aura.setAttachedTo(enchanted.getId());

        castContainmentBreach(aura.getId());

        harness.assertNotOnBattlefield(player2, "Detention Vortex");
        harness.assertInGraveyard(player2, "Detention Vortex");
        harness.assertOnBattlefield(player2, "Zephyr Boots");
        assertThat(createdTokens()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not create a Pest when the target leaves before resolution")
    void doesNotCreatePestWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZephyrBoots());
        prepareContainmentBreach();
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Zephyr Boots");
        harness.assertInGraveyard(player1, "Containment Breach");
        assertThat(createdTokens()).isEmpty();
    }

    @Test
    @DisplayName("The created Pest gains its controller one life when it dies")
    void pestDeathGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZephyrBoots());
        castContainmentBreach(target.getId());
        assertThat(createdTokens()).hasSize(1);
        Permanent pest = createdTokens().getFirst();
        assertThat(pest.getCard().getPower()).isEqualTo(1);
        assertThat(pest.getCard().getToughness()).isEqualTo(1);
        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
        assertThat(pest.getCard().getType()).isEqualTo(CardType.CREATURE);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new MageHuntersOnslaught()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, pest.getId());
        harness.passBothPriorities();

        assertThat(createdTokens()).isEmpty();
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 10);
    }

    private void castContainmentBreach(UUID targetId) {
        prepareContainmentBreach();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void prepareContainmentBreach() {
        harness.setHand(player1, List.of(new ContainmentBreach()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void assertNotOnBattlefield(UUID targetId) {
        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId))
                .doesNotContain(targetId);
    }

    private List<Permanent> createdTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
