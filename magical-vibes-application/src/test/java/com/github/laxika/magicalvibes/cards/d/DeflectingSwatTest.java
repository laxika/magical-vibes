package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeflectingSwat.class, Boomerang.class, GrizzlyBears.class, IcyManipulator.class, EdgarMarkov.class, ProdigalPyromancer.class, Shock.class})
class DeflectingSwatTest extends BaseCardTest {

    @Test
    void changesTargetOfSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void changesTargetOfActivatedAbility() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, pyromancer.getCard().getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        gd.playerCommandZones.get(player2.getId()).add(new EdgarMarkov());
        addCreatureReady(player2, new EdgarMarkov());

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.castInstantWithAlternateCost(player2, 0, shock.getId(), List.of());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        gd.playerCommandZones.get(player2.getId()).add(new EdgarMarkov());
        harness.setHand(player2, List.of(new DeflectingSwat()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
