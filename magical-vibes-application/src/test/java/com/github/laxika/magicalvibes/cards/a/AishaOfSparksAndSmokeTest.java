package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelUltimatum;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AishaOfSparksAndSmoke.class, CruelUltimatum.class, Divination.class,
        GrizzlyBears.class, Shock.class})
class AishaOfSparksAndSmokeTest extends BaseCardTest {

    @Test
    @DisplayName("{R/W} grants first strike until end of turn")
    void firstStrikeGrantedAndWearsOff() {
        Permanent aisha = addCreatureReady(player1, new AishaOfSparksAndSmoke());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage offers a sorcery with mana value at most the damage dealt")
    void combatDamageOffersQualifyingSorcery() {
        Divination divination = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(
                divination, new Shock(), new GrizzlyBears(), new CruelUltimatum())));
        attackAndResolveTrigger();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).isEqualTo("Cast Divination without paying its mana cost?");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(divination.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("Combat damage does not offer instants, creatures, or over-cap sorceries")
    void combatDamageOffersNoNonqualifyingCards() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new Shock(), new GrizzlyBears(), new CruelUltimatum())));
        attackAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void attackAndResolveTrigger() {
        addCreatureReady(player1, new AishaOfSparksAndSmoke());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
    }
}
