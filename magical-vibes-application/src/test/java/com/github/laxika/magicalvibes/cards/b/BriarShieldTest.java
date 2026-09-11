package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RobeOfMirrors;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BriarShield.class, BenalishKnight.class, RobeOfMirrors.class, WindingCanyons.class})
class BriarShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Briar Shield attaches it and gives the creature +1/+1")
    void resolvingAttachesAndBoosts() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        harness.setHand(player1, List.of(new BriarShield()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Briar Shield can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent knight = addCreatureReady(player2, new BenalishKnight());

        harness.setHand(player1, List.of(new BriarShield()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Briar Shield gives the enchanted creature +3/+3 until end of turn")
    void sacrificeBoostsEnchantedCreature() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        Permanent shield = new Permanent(new BriarShield());
        shield.setAttachedTo(knight.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Briar Shield");
        harness.assertNotOnBattlefield(player1, "Briar Shield");
        // Static +1/+1 is gone with the Aura; only the +3/+3 until-EOT boost remains.
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
    }

    @Test
    @DisplayName("The sacrifice ability affects a shrouded enchanted creature")
    void sacrificeAbilityIsNotTargeted() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        Permanent shield = new Permanent(new BriarShield());
        shield.setAttachedTo(knight.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        Permanent robe = new Permanent(new RobeOfMirrors());
        robe.setAttachedTo(knight.getId());
        gd.playerBattlefields.get(player1.getId()).add(robe);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
    }

    @Test
    @DisplayName("The +3/+3 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        Permanent shield = new Permanent(new BriarShield());
        shield.setAttachedTo(knight.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.setHand(player1, List.of(new BriarShield()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent land = findPermanent(player1, "Winding Canyons");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
