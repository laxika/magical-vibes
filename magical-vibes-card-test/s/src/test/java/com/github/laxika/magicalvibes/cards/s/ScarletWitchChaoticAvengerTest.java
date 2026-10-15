package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroInTraining;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarletWitchChaoticAvenger.class, HeroInTraining.class, Divination.class,
        Forest.class, GrizzlyBears.class})
class ScarletWitchChaoticAvengerTest extends BaseCardTest {

    @Test
    void exilesTwoCardsFaceDownWithScarletWitchAndOffersHero() {
        Permanent scarletWitch = addAttackingScarletWitch();
        HeroInTraining hero = new HeroInTraining();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hero, creature));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(scarletWitch.getId()))
                .extracting(Card::getId)
                .containsExactly(hero.getId(), creature.getId());
        assertThat(gd.findExiledCard(hero.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(creature.getId()).faceDown()).isTrue();
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(hero.getId());
    }

    @Test
    void offersNoncreatureSpellButNotNonHeroCreature() {
        addAttackingScarletWitch();
        Divination divination = new Divination();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(divination, creature, new Forest(), new Forest()));

        resolveCombatAndTrigger();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(divination.getId());

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(divination);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void canCastHeroWithoutManaAndLeavesOtherCardExiled() {
        addAttackingScarletWitch();
        HeroInTraining hero = new HeroInTraining();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(hero, land, new Forest()));

        resolveCombatAndTrigger();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(hero.getId());
        assertThat(interaction.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(hero.getId());
        assertThat(gd.findExiledCard(hero.getId())).isNull();
        assertThat(gd.findExiledCard(land.getId()).faceDown()).isTrue();
    }

    @Test
    void laterTriggerOffersPreviouslyExiledSpellAlongsideNewSpell() {
        Permanent scarletWitch = addAttackingScarletWitch();
        HeroInTraining earlierHero = new HeroInTraining();
        HeroInTraining laterHero = new HeroInTraining();
        harness.setLibrary(player1, List.of(earlierHero, new Forest(), laterHero, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.findExiledCard(earlierHero.getId()).faceDown()).isTrue();

        scarletWitch.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombatAndTrigger();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds())
                .containsExactlyInAnyOrder(earlierHero.getId(), laterHero.getId());
        assertThat(interaction.maxCount()).isEqualTo(1);
    }

    @Test
    void emptyLibraryStillAllowsCastingPreviouslyExiledSpell() {
        Permanent scarletWitch = addAttackingScarletWitch();
        HeroInTraining hero = new HeroInTraining();
        harness.setLibrary(player1, List.of(hero));

        resolveCombatAndTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        scarletWitch.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(hero.getId());
    }

    private Permanent addAttackingScarletWitch() {
        Permanent scarletWitch = addCreatureReady(player1, new ScarletWitchChaoticAvenger());
        scarletWitch.setAttacking(true);
        return scarletWitch;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
