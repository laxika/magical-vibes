package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ImprisonedInTheMoon.class, FieldOfRuin.class, Forest.class, GrizzlyBears.class,
        JaceBeleren.class, LightningBolt.class, Plains.class})
class ImprisonedInTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to target creature")
    void resolvingAttachesToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature can tap for colorless mana via granted ability")
    void enchantedCreatureTapsForColorless() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted Plains keeps Plains subtype but only taps for colorless")
    void enchantedPlainsKeepsSubtypeProducesColorlessOnly() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(plains.getId());

        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, plains, CardSubtype.PLAINS)).isTrue();

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
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Rejects targeting a noncreature nonland nonplaneswalker")
    void rejectsIllegalTarget() {
        Permanent auraTarget = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());

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

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(bearsId);
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

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(jaceId);

        assertThat(gqs.isPlaneswalker(gd, gqs.findPermanentById(gd, jaceId))).isFalse();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, jaceId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the aura restores the creature")
    void removingAuraRestoresCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.isCreature(gd, bears)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Losing the creature type also removes its creature subtypes")
    void enchantedCreatureLosesCreatureSubtypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isFalse();
    }

    @Test
    @DisplayName("Losing the planeswalker type also removes its planeswalker subtype")
    void enchantedPlaneswalkerLosesPlaneswalkerSubtype() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, jace.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, jace)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, jace, CardSubtype.JACE)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, jace, CardSupertype.LEGENDARY)).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("A newly controlled creature can tap for mana once it becomes a noncreature land")
    void newlyControlledCreatureCanTapForMana() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Aura resolves on a land and its controller receives the granted mana")
    void resolvesOnOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }
}
