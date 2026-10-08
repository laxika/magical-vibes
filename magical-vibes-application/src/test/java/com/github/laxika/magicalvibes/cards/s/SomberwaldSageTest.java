package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TravelPreparations;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SomberwaldSage.class, TravelPreparations.class})
class SomberwaldSageTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping prompts for a mana color choice without using the stack")
    void tappingPromptsColorChoice() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds three creature-spell-only mana")
    void choosingColorAddsThreeCreatureSpellMana() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);
        harness.setHand(player1, List.of(new SomberwaldSage()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell")
    void manaCanCastCreatureSpell() {
        harness.addToBattlefield(player1, new SomberwaldSage());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.GREEN, 3);

        SomberwaldSage beast = new SomberwaldSage();
        harness.setHand(player1, List.of(beast));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(beast);
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        harness.addToBattlefield(player1, new SomberwaldSage());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.GREEN, 3);

        harness.setHand(player1, List.of(new TravelPreparations()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0,
                List.of(harness.getPermanentId(player1, "Somberwald Sage"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void producedManaPaysColoredAndGenericCreatureCosts() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);
        harness.setHand(player1, List.of(new SomberwaldSage()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void producedManaCannotPayGenericNonCreatureCosts() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);
        harness.setHand(player1, List.of(new TravelPreparations()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, List.of(sage.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(3);
    }

    @Test
    void summoningSickSageCannotActivate() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sage.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
    }

    @Test
    void tappedSageCannotActivateAgain() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED))
                .isEqualTo(3);
    }
}
