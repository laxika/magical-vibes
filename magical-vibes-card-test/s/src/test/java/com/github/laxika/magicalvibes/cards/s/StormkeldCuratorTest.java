package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherTunnel;
import com.github.laxika.magicalvibes.cards.a.AllThatGlitters;
import com.github.laxika.magicalvibes.cards.c.CelestialMantle;
import com.github.laxika.magicalvibes.cards.c.CuratorsWard;
import com.github.laxika.magicalvibes.cards.c.Curiosity;
import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.cards.f.FaceOfDivinity;
import com.github.laxika.magicalvibes.cards.k.KnightlyValor;
import com.github.laxika.magicalvibes.cards.m.MetamorphicAlteration;
import com.github.laxika.magicalvibes.cards.o.OnSerrasWings;
import com.github.laxika.magicalvibes.cards.q.QuintoriusFieldHistorian;
import com.github.laxika.magicalvibes.cards.r.RousingRead;
import com.github.laxika.magicalvibes.cards.s.StaggeringInsight;
import com.github.laxika.magicalvibes.cards.t.TakeFlight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormkeldCurator.class, AetherTunnel.class, AllThatGlitters.class, CelestialMantle.class,
        CuratorsWard.class, Curiosity.class, EtherealArmor.class, FaceOfDivinity.class,
        KnightlyValor.class, MetamorphicAlteration.class, OnSerrasWings.class,
        RousingRead.class, StaggeringInsight.class, TakeFlight.class, QuintoriusFieldHistorian.class})
class StormkeldCuratorTest extends BaseCardTest {

    @Test
    void entersAndOffersAurasFromHandAndGraveyard() {
        StormkeldCurator curator = new StormkeldCurator();
        AetherTunnel handAura = new AetherTunnel();
        Curiosity graveyardAura = new Curiosity();
        harness.setHand(player1, List.of(curator, handAura));
        harness.setGraveyard(player1, List.of(graveyardAura));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(handAura.getId(), graveyardAura.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(handAura.getId(), graveyardAura.getId()));

        Permanent curatorPermanent = findPermanent(player1, "Stormkeld Curator");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isAttached)
                .allMatch(permanent -> permanent.getAttachedTo().equals(curatorPermanent.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void adventureConjuresXRandomSpellbookCardsIntoHand() {
        StormkeldCurator card = new StormkeldCurator();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, 2, Map.of());
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(2);
        assertThat(hand).extracting(Card::getName).allMatch(name -> List.of(
                "Aether Tunnel", "All That Glitters", "Celestial Mantle", "Curator's Ward",
                "Curiosity", "Ethereal Armor", "Face of Divinity", "Knightly Valor",
                "Metamorphic Alteration", "On Serra's Wings", "Rousing Read",
                "Staggering Insight", "Take Flight").contains(name));
    }

    @Test
    void mayDeclineAllAuras() {
        AetherTunnel handAura = new AetherTunnel();
        AetherTunnel graveyardAura = new AetherTunnel();
        harness.setHand(player1, List.of(new StormkeldCurator(), handAura));
        harness.setGraveyard(player1, List.of(graveyardAura));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handAura);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardAura);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChooseOnlyOneAuraAndDoesNotOfferOpponentsCardsOrNonAuras() {
        AetherTunnel handAura = new AetherTunnel();
        AetherTunnel graveyardAura = new AetherTunnel();
        StormkeldCurator nonAura = new StormkeldCurator();
        AetherTunnel opponentAura = new AetherTunnel();
        harness.setHand(player1, List.of(new StormkeldCurator(), handAura, nonAura));
        harness.setGraveyard(player1, List.of(graveyardAura));
        harness.setHand(player2, List.of(opponentAura));
        harness.setGraveyard(player2, List.of(new AetherTunnel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(handAura.getId(), graveyardAura.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardAura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handAura, nonAura);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Aether Tunnel").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Stormkeld Curator").getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentAura);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void returningMultipleAurasIsOneGraveyardDepartureEvent() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        AetherTunnel first = new AetherTunnel();
        AetherTunnel second = new AetherTunnel();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new StormkeldCurator()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(findPermanents(player1, "Aether Tunnel")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void zeroValueAdventureConjuresNothingAndAllowsCreatureCastFromExile() {
        StormkeldCurator card = new StormkeldCurator();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormkeld Curator");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
