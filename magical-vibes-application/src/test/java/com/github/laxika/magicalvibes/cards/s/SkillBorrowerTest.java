package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodpyreElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkillBorrower.class, Forest.class, GrizzlyBears.class, ProdigalPyromancer.class,
        RodOfRuin.class, LlanowarElves.class, ObeliskOfEsper.class, BloodpyreElemental.class})
class SkillBorrowerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated ability while top library card is a creature")
    void gainsAbilityFromCreatureOnTop() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new ProdigalPyromancer(), new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Gains activated ability while top library card is a noncreature artifact")
    void gainsAbilityFromArtifactOnTop() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new RodOfRuin(), new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
    }

    @Test
    @DisplayName("Gains no abilities while top library card is neither artifact nor creature")
    void noAbilityFromNonArtifactNonCreatureOnTop() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Gains no abilities from a vanilla creature on top")
    void noAbilityFromVanillaCreatureOnTop() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Abilities change as the top card changes")
    void abilitiesTrackTopCard() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).isEmpty();

        gd.playerDecks.get(player1.getId()).addFirst(new ProdigalPyromancer());
        assertThat(gqs.computeStaticBonus(gd, borrower).grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Can activate a tap ability gained from the top creature card")
    void canActivateGainedTapAbility() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
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
        addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, new ArrayList<>(List.of(new ProdigalPyromancer(), new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skill Borrower");
    }

    @Test
    void gainsTapManaAbilityStoredAsTapEffect() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        Card top = new LlanowarElves();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(borrower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void gainsArtifactManaAbilityWithColorChoice() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, List.of(new ObeliskOfEsper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, BLUE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(borrower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void artifactAbilityStillRequiresItsManaCost() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, List.of(new RodOfRuin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(borrower.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(borrower.isTapped()).isTrue();
    }

    @Test
    void cannotUseGainedTapAbilityWhileSummoningSick() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        borrower.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new ProdigalPyromancer()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(borrower.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void activatedAbilityStillResolvesAfterTopCardChanges() {
        addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, List.of(new ProdigalPyromancer()));
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryGrantsNoActivatedAbility() {
        addCreatureReady(player1, new SkillBorrower());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeCostSacrificesBorrowerAndLeavesTopCardInLibrary() {
        Permanent borrower = addCreatureReady(player1, new SkillBorrower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card top = new BloodpyreElemental();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(borrower);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(borrower.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void revealsControllerTopCardToBothPlayersAndTracksChanges() {
        harness.addToBattlefield(player1, new SkillBorrower());
        Card first = new Forest();
        Card next = new GrizzlyBears();
        Card opponentsTop = new RodOfRuin();
        harness.setLibrary(player1, List.of(first, next));
        harness.setLibrary(player2, List.of(opponentsTop));
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains(first.getId().toString())
                            && !message.contains(opponentsTop.getId().toString()));
        }

        harness.setLibrary(player1, List.of(next));
        harness.clearMessages();
        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains(next.getId().toString())
                            && !message.contains(first.getId().toString()));
        }
    }

    @Test
    void borrowedSorcerySpeedAbilityCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new SkillBorrower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new BloodpyreElemental()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Skill Borrower");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesControllersLibraryRatherThanOpponentsLibrary() {
        Permanent borrower = addCreatureReady(player2, new SkillBorrower());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new ProdigalPyromancer()));

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(borrower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
