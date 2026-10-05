package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.c.CradleToGrave;
import com.github.laxika.magicalvibes.cards.k.KorDirge;
import com.github.laxika.magicalvibes.cards.p.PiracyCharm;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Sunlance;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuckDrubb.class, CitanulWoodreaders.class, CradleToGrave.class, KorDirge.class,
        PiracyCharm.class, ProdigalPyromancer.class, Sunlance.class, SeedsOfStrength.class})
class MuckDrubbTest extends BaseCardTest {

    @Test
    @DisplayName("ETB changes a single creature-targeting spell to Muck Drubb")
    void redirectsSingleCreatureTargetingSpellToItself() {
        Permanent woodreaders = addCreatureReady(player1, new CitanulWoodreaders());
        Sunlance sunlance = new Sunlance();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(sunlance));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castSorcery(player2, 0, woodreaders.getId());
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.castFromHand(player1, drubb, "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sunlance.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Citanul Woodreaders");
        harness.assertNotOnBattlefield(player1, "Muck Drubb");
    }

    @Test
    @DisplayName("A player-targeting spell is not a legal ETB target")
    void doesNotTargetPlayerSpell() {
        PiracyCharm piracyCharm = new PiracyCharm();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(piracyCharm));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castModalInstant(player2, 0, 2, List.of(player1.getId()));
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb, new CitanulWoodreaders()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Muck Drubb");
        harness.assertInGraveyard(player1, "Citanul Woodreaders");
    }

    @Test
    @DisplayName("Repeated target occurrences for one creature are all changed")
    void redirectsRepeatedTargetOccurrences() {
        Permanent woodreaders = addCreatureReady(player1, new CitanulWoodreaders());
        SeedsOfStrength seeds = new SeedsOfStrength();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(seeds));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, List.of(woodreaders.getId(), woodreaders.getId(), woodreaders.getId()));
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.castFromHand(player1, drubb, "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, seeds.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent drubbPermanent = findPermanent(player1, "Muck Drubb");
        assertThat(gqs.getEffectivePower(gd, drubbPermanent)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, drubbPermanent)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);
    }

    @Test
    @DisplayName("A spell with multiple distinct creature targets is not a legal ETB target")
    void doesNotTargetSpellWithMultipleDistinctCreatureTargets() {
        Permanent player2Creature = addCreatureReady(player2, new CitanulWoodreaders());
        Permanent player1Creature = addCreatureReady(player1, new CitanulWoodreaders());
        Permanent damageSource = addCreatureReady(player2, new ProdigalPyromancer());
        KorDirge korDirge = new KorDirge();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(korDirge));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, List.of(player2Creature.getId(), player1Creature.getId()));
        harness.passPriority(player2);

        harness.castFromHand(player1, new MuckDrubb(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Muck Drubb");
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, damageSource.getId());
    }

    @Test
    @DisplayName("A spell cannot be redirected when Muck Drubb is an illegal replacement target")
    void leavesNonblackRestrictionSpellUnchanged() {
        Permanent woodreaders = harness.enterBattlefieldAndReturn(player1, new CitanulWoodreaders());
        CradleToGrave removal = new CradleToGrave();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(removal));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, woodreaders.getId());
        harness.passPriority(player2);

        harness.castFromHand(player1, new MuckDrubb(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, removal.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Muck Drubb");
        harness.assertNotOnBattlefield(player1, "Citanul Woodreaders");
        harness.assertInGraveyard(player1, "Citanul Woodreaders");
    }

    @Test
    @DisplayName("A creature-targeting activated ability is not a spell")
    void doesNotRedirectActivatedAbility() {
        Permanent woodreaders = addCreatureReady(player1, new CitanulWoodreaders());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, woodreaders.getId());
        harness.passPriority(player2);

        harness.castFromHand(player1, new MuckDrubb(), "{3}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(woodreaders.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Muck Drubb").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining madness moves Muck Drubb from exile to the graveyard")
    void declinesMadness() {
        MuckDrubb drubb = discardDrubb();
        assertThat(gd.findExiledCard(drubb.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Muck Drubb");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Muck Drubb");
        harness.assertNotOnBattlefield(player1, "Muck Drubb");
        assertThat(gd.findExiledCard(drubb.getId())).isNull();
    }

    @Test
    @DisplayName("Madness allows Muck Drubb to be cast after it is discarded")
    void castsForMadness() {
        MuckDrubb drubb = discardDrubb();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(drubb.getId()));
    }

    private MuckDrubb discardDrubb() {
        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb));
        harness.setHand(player2, List.of(new PiracyCharm()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castModalInstant(player2, 0, 2, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return drubb;
    }
}
