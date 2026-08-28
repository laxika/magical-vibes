package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeylineImmersion.class, IsamaruHoundOfKonda.class, GrizzlyBears.class,
        Shock.class, FountainOfYouth.class})
class LeylineImmersionTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline Immersion can target a legendary creature")
    void canTargetLegendaryCreature() {
        Permanent isamaru = addReadyCreature(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new LeylineImmersion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, isamaru.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Leyline Immersion cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeylineImmersion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Enchanted creature has ward {2}")
    void wardCountersUnpaidSpell() {
        Permanent isamaru = addReadyCreature(player1, new IsamaruHoundOfKonda());
        addAura(isamaru);

        castShockAt(player2, isamaru, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(isamaru);
    }

    @Test
    @DisplayName("Enchanted creature's ward lets a paid spell resolve")
    void wardAllowsPaidSpell() {
        Permanent isamaru = addReadyCreature(player1, new IsamaruHoundOfKonda());
        addAura(isamaru);

        castShockAt(player2, isamaru, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("Enchanted creature can add five mana in any combination of colors")
    void addsFiveSpellOnlyMana() {
        Permanent isamaru = addReadyCreature(player1, new IsamaruHoundOfKonda());
        addAura(isamaru);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getSpellOnlyManaTotal()).isEqualTo(5);
        assertThat(pool.getTotal()).isEqualTo(5);
        assertThat(pool.getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Spell-only mana pays for spells but not activated abilities")
    void spellOnlyManaRestriction() {
        Permanent isamaru = addReadyCreature(player1, new IsamaruHoundOfKonda());
        addAura(isamaru);
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAura(Permanent enchantedCreature) {
        Permanent aura = new Permanent(new LeylineImmersion());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void castShockAt(Player caster, Permanent target, int redMana) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, redMana);
        harness.castInstant(caster, 0, target.getId());
    }
}
