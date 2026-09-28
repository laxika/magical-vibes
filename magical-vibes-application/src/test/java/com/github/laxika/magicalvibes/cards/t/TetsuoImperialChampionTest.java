package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlateArmor;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TetsuoImperialChampion.class, PlateArmor.class, LeoninScimitar.class,
        GrizzlyBears.class, CounselOfTheSoratami.class, Tidings.class, CrawWurm.class})
class TetsuoImperialChampionTest extends BaseCardTest {

    private static final String DAMAGE_MODE =
            "Tetsuo deals damage equal to the greatest mana value among Equipment attached to it to any target";
    private static final String SPELL_MODE =
            "You may cast an instant or sorcery spell from your hand with mana value less than or equal to the greatest mana value among Equipment attached to Tetsuo without paying its mana cost";

    @Test
    @DisplayName("Attack trigger deals damage equal to the greatest attached Equipment mana value")
    void dealsGreatestAttachedEquipmentManaValueAsDamage() {
        Permanent tetsuo = addCreatureReady(player1, new TetsuoImperialChampion());
        Permanent higherEquipment = harness.addToBattlefieldAndReturn(player2, new PlateArmor());
        Permanent lowerEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        higherEquipment.setAttachedTo(tetsuo.getId());
        lowerEquipment.setAttachedTo(tetsuo.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, DAMAGE_MODE);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack trigger offers only eligible spells within the attached Equipment mana value")
    void offersEligibleSpellFromHand() {
        Permanent tetsuo = addCreatureReady(player1, new TetsuoImperialChampion());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(tetsuo.getId());
        CounselOfTheSoratami eligibleSpell = new CounselOfTheSoratami();
        Tidings tooExpensiveSpell = new Tidings();
        CrawWurm wrongType = new CrawWurm();
        harness.setHand(player1, List.of(tooExpensiveSpell, wrongType, eligibleSpell));

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, SPELL_MODE);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("Counsel of the Soratami")
                .doesNotContain("Tidings")
                .doesNotContain("Craw Wurm");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligibleSpell.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensiveSpell, wrongType);
    }

    @Test
    @DisplayName("Attack trigger does not fire when Tetsuo is not equipped")
    void doesNotTriggerWithoutEquipment() {
        addCreatureReady(player1, new TetsuoImperialChampion());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveCombatAndTrigger() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }
}
