package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkillBorrowerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated ability while top library card is a creature")
    void gainsAbilityFromCreatureOnTop() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new ProdigalPyromancer(), new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Gains activated ability while top library card is a noncreature artifact")
    void gainsAbilityFromArtifactOnTop() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new RodOfRuin(), new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
    }

    @Test
    @DisplayName("Gains no abilities while top library card is neither artifact nor creature")
    void noAbilityFromNonArtifactNonCreatureOnTop() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Gains no abilities from a vanilla creature on top")
    void noAbilityFromVanillaCreatureOnTop() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Abilities change as the top card changes")
    void abilitiesTrackTopCard() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).isEmpty();

        gd.playerDecks.get(player1.getId()).addFirst(new ProdigalPyromancer());
        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Can activate a tap ability gained from the top creature card")
    void canActivateGainedTapAbility() {
        Permanent borrower = addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new ProdigalPyromancer(), new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(borrower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability on the stack uses Skill Borrower's name")
    void gainedAbilityUsesBorrowerName() {
        addBorrowerReady(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new ProdigalPyromancer(), new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skill Borrower");
    }

    private Permanent addBorrowerReady(Player player) {
        SkillBorrower card = new SkillBorrower();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
