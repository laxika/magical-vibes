package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.h.HulkingMetamorph;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDragonEngine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishraClaimedByGix.class, MishraLostToPhyrexia.class, PhyrexianDragonEngine.class,
        ArgothianSprite.class, MachineOverMatter.class, HulkingMetamorph.class})
class MishraClaimedByGixTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two creatures drains each opponent for two and gains two life")
    void attacksDrainForNumberOfAttackers() {
        Permanent mishra = addCreatureReady(player1, new MishraClaimedByGix());
        Permanent bear = addCreatureReady(player1, new ArgothianSprite());
        addCreatureReady(player2, new ArgothianSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(mishra),
                gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Melds attacking Mishra with an attacking Phyrexian Dragon Engine")
    void meldsWhenBothPartsAttack() {
        Permanent mishra = addCreatureReady(player1, new MishraClaimedByGix());
        Permanent dragonEngine = addCreatureReady(player1, new PhyrexianDragonEngine());
        Permanent ownCreature = addCreatureReady(player1, new ArgothianSprite());
        addCreatureReady(player2, new ArgothianSprite());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(mishra),
                gd.playerBattlefields.get(player1.getId()).indexOf(dragonEngine)));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mishra deals 3 damage to any target");
        harness.handleListChoice(player1,
                "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");

        Permanent melded = findPermanent(player1, "Mishra, Lost to Phyrexia");
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(melded.isTapped()).isTrue();
        assertThat(melded.isAttacking()).isTrue();

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
    }

    @Test
    @DisplayName("Mishra's back face resolves exactly three distinct modes")
    void backFaceChoosesThreeDistinctModes() {
        Permanent ownCreature = addCreatureReady(player1, new ArgothianSprite());
        Permanent opposingCreature = addCreatureReady(player2, new ArgothianSprite());
        Permanent mishra = addCreatureReady(player1, new MishraLostToPhyrexia());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mishra)));
        resolveAllTriggers();
        chooseNonTargetingBackModes();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
        assertThat(findPermanents(player1, "Powerstone")).allMatch(Permanent::isTapped);
    }

    @Test
    void drainsWhenMishraDoesNotAttack() {
        addCreatureReady(player1, new MishraClaimedByGix());
        addCreatureReady(player1, new ArgothianSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Mishra, Claimed by Gix");
    }

    @Test
    void countsAttackersAtResolutionAfterOneIsReturnedToHand() {
        addCreatureReady(player1, new MishraClaimedByGix());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(List.of(0, 1));
        harness.castInstant(player1, 0, sprite.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Argothian Sprite");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void removedPartnerPreventsMeldButDoesNotPreventDrain() {
        addCreatureReady(player1, new MishraClaimedByGix());
        Permanent partner = addCreatureReady(player1, new PhyrexianDragonEngine());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0, 1));
        harness.castInstant(player1, 0, partner.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Phyrexian Dragon Engine");
        harness.assertOnBattlefield(player1, "Mishra, Claimed by Gix");
        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotMeldWithNonattackingPartner() {
        addCreatureReady(player1, new MishraClaimedByGix());
        addCreatureReady(player1, new PhyrexianDragonEngine());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mishra, Claimed by Gix");
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
    }

    @Test
    void doesNotMeldWithPartnerOwnedByOpponent() {
        addCreatureReady(player1, new MishraClaimedByGix());
        PhyrexianDragonEngine stolen = new PhyrexianDragonEngine();
        stolen.setOwnerId(player2.getId());
        addCreatureReady(player1, stolen);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mishra, Claimed by Gix");
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
    }

    @Test
    void meldsWithAttackingPartnerInsteadOfEarlierNonattackingPartner() {
        Permanent mishra = addCreatureReady(player1, new MishraClaimedByGix());
        Permanent idlePartner = addCreatureReady(player1, new PhyrexianDragonEngine());
        Permanent attackingPartner = addCreatureReady(player1, new PhyrexianDragonEngine());

        declareAttackers(List.of(0, 2));
        resolveAllTriggers();
        chooseNonTargetingBackModes();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(idlePartner).doesNotContain(mishra, attackingPartner);
        assertThat(findPermanent(player1, "Mishra, Lost to Phyrexia").getMeldComponentCards())
                .contains(attackingPartner.getOriginalCard()).doesNotContain(idlePartner.getOriginalCard());
    }

    @Test
    void tokenPartnerIsExiledButCannotMeld() {
        addCreatureReady(player1, new MishraClaimedByGix());
        PhyrexianDragonEngine tokenCopy = new PhyrexianDragonEngine();
        tokenCopy.setToken(true);
        addCreatureReady(player1, tokenCopy);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
        assertThat(findPermanents(player1, "Mishra, Claimed by Gix")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Dragon Engine")).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Mishra, Claimed by Gix"));
    }

    @Test
    void backFaceAttackModesAreChosenBeforePassingPriority() {
        addCreatureReady(player1, new MishraLostToPhyrexia());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        chooseNonTargetingBackModes();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
    }

    @Test
    void backFaceTargetedModesDiscardDamageAndDestroy() {
        addCreatureReady(player1, new MishraLostToPhyrexia());
        Permanent artifact = addCreatureReady(player2, new PhyrexianDragonEngine());
        harness.setHand(player2, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Target opponent discards two cards");
        harness.handleListChoice(player1, "Mishra deals 3 damage to any target");
        harness.handleListChoice(player1, "Destroy target artifact or planeswalker");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Phyrexian Dragon Engine");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Argothian Sprite")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking without Mishra does not meld but still drains life")
    void attackingOnlyWithDragonEngineDoesNotMeld() {
        Permanent mishra = addCreatureReady(player1, new MishraClaimedByGix());
        Permanent engine = addCreatureReady(player1, new PhyrexianDragonEngine());
        addCreatureReady(player2, new ArgothianSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(engine)));
        resolveAllTriggers();

        assertThat(gqs.findPermanentById(gd, mishra.getId())).isSameAs(mishra);
        assertThat(gqs.findPermanentById(gd, engine.getId())).isSameAs(engine);
        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A copied Dragon Engine is exiled but cannot meld")
    void copiedDragonEngineCannotMeld() {
        Permanent mishra = addCreatureReady(player1, new MishraClaimedByGix());
        Permanent realEngine = addCreatureReady(player1, new PhyrexianDragonEngine());
        addCreatureReady(player2, new ArgothianSprite());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.castFromHand(player1, metamorph, "{9}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, realEngine.getId());
        resolveAllTriggers();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getId().equals(metamorph.getId()))
                .findFirst().orElseThrow();
        copy.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(realEngine);
        gd.playerBattlefields.get(player1.getId()).add(realEngine);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(mishra),
                gd.playerBattlefields.get(player1.getId()).indexOf(copy)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mishra, Lost to Phyrexia")).isEmpty();
        assertThat(gqs.findPermanentById(gd, mishra.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, copy.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, realEngine.getId())).isSameAs(realEngine);
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(mishra.getOriginalCard().getId()));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(metamorph.getId()));
    }

    @Test
    void meldingUnearthedEngineDoesNotExileTheResultAtEndStep() {
        addCreatureReady(player1, new MishraClaimedByGix());
        harness.setGraveyard(player1, List.of(new PhyrexianDragonEngine()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 40);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        chooseNonTargetingBackModes();
        resolveAllTriggers();

        Permanent melded = findPermanent(player1, "Mishra, Lost to Phyrexia");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gqs.findPermanentById(gd, melded.getId())).isSameAs(melded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void returningMeldedResultToHandReturnsBothFrontFaces() {
        addCreatureReady(player1, new MishraClaimedByGix());
        addCreatureReady(player1, new PhyrexianDragonEngine());
        harness.setLife(player2, 40);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        chooseNonTargetingBackModes();
        resolveAllTriggers();
        Permanent melded = findPermanent(player1, "Mishra, Lost to Phyrexia");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, melded.getId());

        harness.assertNotOnBattlefield(player1, "Mishra, Lost to Phyrexia");
        harness.assertInHand(player1, "Mishra, Claimed by Gix");
        harness.assertInHand(player1, "Phyrexian Dragon Engine");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void chooseNonTargetingBackModes() {
        harness.handleListChoice(player1,
                "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1,
                "Creatures you don't control get -1/-1 until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");
    }

}
