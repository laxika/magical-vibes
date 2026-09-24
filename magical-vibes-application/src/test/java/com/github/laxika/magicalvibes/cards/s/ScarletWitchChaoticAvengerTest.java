package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarletWitchChaoticAvenger.class, BenalishHero.class, Divination.class,
        Forest.class, GrizzlyBears.class})
class ScarletWitchChaoticAvengerTest extends BaseCardTest {

    @Test
    void exilesTwoCardsFaceDownWithScarletWitchAndOffersHero() {
        Permanent scarletWitch = addAttackingScarletWitch();
        BenalishHero hero = new BenalishHero();
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
