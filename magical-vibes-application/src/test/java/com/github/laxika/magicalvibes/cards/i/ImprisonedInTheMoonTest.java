package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImprisonedInTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to target creature")
    void resolvingAttachesToCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Imprisoned in the Moon")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature is a colorless land, not a creature")
    void enchantedCreatureBecomesColorlessLand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature can tap for colorless mana via granted ability")
    void enchantedCreatureTapsForColorless() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted Plains keeps Plains subtype but only taps for colorless")
    void enchantedPlainsKeepsSubtypeProducesColorlessOnly() {
        Permanent plains = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(plains);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(plains.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, plains).grantedSubtypes())
                .doesNotContain(CardSubtype.PLAINS);
        // Printed Plains subtype retained (not overridden)
        assertThat(plains.getCard().getSubtypes()).contains(CardSubtype.PLAINS);

        // Intrinsic white mana is gone
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Forest retains Forest subtype and produces colorless only")
    void enchantedForestProducesColorlessOnly() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Rejects targeting a noncreature nonland nonplaneswalker")
    void rejectsIllegalTarget() {
        Permanent auraTarget = new Permanent(new ImprisonedInTheMoon());
        // Use another Imprisoned in the Moon as an enchantment permanent on the battlefield
        gd.playerBattlefields.get(player2.getId()).add(auraTarget);

        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, auraTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * CR 613.1d (layer 4, type-changing effects) is applied before target legality is judged, so a
     * creature this aura turned into a land <em>is</em> a legal "target land" — even though its
     * printed type line says otherwise. Field of Ruin's "destroy target nonbasic land an opponent
     * controls" is the reader.
     */
    @Test
    @DisplayName("Enchanted creature is a legal target for 'destroy target land'")
    void enchantedCreatureIsALegalLandTarget() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bearsId);
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    /**
     * The mirror case: "any target" is a creature, planeswalker, player or battle (CR 115.4), judged
     * after layer 4. A planeswalker this aura turned into a colorless land is none of those, so
     * Lightning Bolt can no longer be pointed at it.
     */
    @Test
    @DisplayName("Enchanted planeswalker is no longer a legal 'any target'")
    void enchantedPlaneswalkerIsNotAnAnyTarget() {
        harness.addToBattlefield(player2, new JaceBeleren());
        UUID jaceId = harness.getPermanentId(player2, "Jace Beleren");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(jaceId);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isPlaneswalker(gd, gqs.findPermanentById(gd, jaceId))).isFalse();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, jaceId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the aura restores the creature")
    void removingAuraRestoresCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isCreature(gd, bears)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN);
    }
}
