package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CaseOfTheUneatenFeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReliveThePast.class, TormodsCrypt.class, Forest.class, PhyrexianArena.class, Pacifism.class,
        CaseOfTheUneatenFeast.class, MagnifyingGlass.class})
class ReliveThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one artifact, land, and non-Aura enchantment as 5/5 Elementals")
    void returnsEachTargetTypeAndAnimatesThem() {
        TormodsCrypt artifact = new TormodsCrypt();
        Forest land = new Forest();
        PhyrexianArena enchantment = new PhyrexianArena();
        Pacifism aura = new Pacifism();
        harness.setGraveyard(player1, List.of(artifact, land, enchantment, aura));

        castReliveThePast();
        choose(artifact);
        choose(land);

        PendingInteraction.MultiGraveyardChoice finalChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(finalChoice.validCardIds()).containsExactly(enchantment.getId());
        assertThat(finalChoice.validCardIds()).doesNotContain(aura.getId());
        choose(enchantment);
        harness.passBothPriorities();

        Permanent returnedArtifact = returnedPermanent(artifact);
        Permanent returnedLand = returnedPermanent(land);
        Permanent returnedEnchantment = returnedPermanent(enchantment);
        for (Permanent returned : List.of(returnedArtifact, returnedLand, returnedEnchantment)) {
            assertThat(gqs.isCreature(gd, returned)).isTrue();
            assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.ELEMENTAL);
            assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
        }
        assertThat(gqs.isArtifact(returnedArtifact)).isTrue();
        assertThat(gqs.isLand(gd, returnedLand)).isTrue();
        assertThat(gqs.isEnchantment(gd, returnedEnchantment)).isTrue();
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Each target group is optional")
    void canDeclineEveryTargetGroup() {
        TormodsCrypt artifact = new TormodsCrypt();
        Forest land = new Forest();
        PhyrexianArena enchantment = new PhyrexianArena();
        harness.setGraveyard(player1, List.of(artifact, land, enchantment));

        castReliveThePast();
        chooseNothing();
        chooseNothing();
        chooseNothing();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Tormod's Crypt", "Forest", "Phyrexian Arena");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> List.of(artifact, land, enchantment).stream()
                        .anyMatch(card -> card.getId().equals(permanent.getCard().getId())));
    }

    @Test
    @DisplayName("Returned noncreatures enter as creatures and trigger life gain")
    void returnedPermanentsEnterAsCreatures() {
        harness.addToBattlefield(player1, new CaseOfTheUneatenFeast());
        MagnifyingGlass artifact = new MagnifyingGlass();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(artifact, land));

        castReliveThePast();
        choose(artifact);
        choose(land);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gqs.isCreature(gd, returnedPermanent(artifact))).isTrue();
        assertThat(gqs.isCreature(gd, returnedPermanent(land))).isTrue();
        assertThat(findPermanents(player1, "Case of the Uneaten Feast")).hasSize(1);
    }

    @Test
    @DisplayName("An illegal target already on the battlefield is not animated")
    void doesNotAnimateTargetReturnedByAnotherEffect() {
        MagnifyingGlass artifact = new MagnifyingGlass();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(artifact, land));

        castReliveThePast();
        choose(artifact);
        choose(land);

        // Model another effect returning the artifact while this spell is on the stack.
        harness.setGraveyard(player1, List.of(land));
        Permanent independentlyReturned = harness.addToBattlefieldAndReturn(player1, artifact);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, independentlyReturned)).isFalse();
        assertThat(gqs.isCreature(gd, returnedPermanent(land))).isTrue();
        assertThat(gqs.getEffectivePower(gd, returnedPermanent(land))).isEqualTo(5);
    }

    @Test
    @DisplayName("The spell does not resolve when its only target leaves the graveyard")
    void doesNotResolveWhenAllTargetsAreIllegal() {
        MagnifyingGlass artifact = new MagnifyingGlass();
        harness.setGraveyard(player1, List.of(artifact));

        castReliveThePast();
        choose(artifact);
        harness.setGraveyard(player1, List.of());
        Permanent independentlyReturned = harness.addToBattlefieldAndReturn(player1, artifact);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, independentlyReturned)).isFalse();
        harness.assertInGraveyard(player1, "Relive the Past");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can resolve with an empty graveyard and no targets")
    void resolvesWithNoAvailableTargets() {
        harness.setGraveyard(player1, List.of());

        castReliveThePast();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Relive the Past");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castReliveThePast() {
        harness.setHand(player1, List.of(new ReliveThePast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private void choose(Card card) {
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    private void chooseNothing() {
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    private Permanent returnedPermanent(Card card) {
        return findPermanent(player1, card.getName());
    }
}
