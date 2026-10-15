package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.cards.s.SculptorOfWinter;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.cards.v.ValkiGodOfLies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InSearchOfGreatness.class, GrizzlyBears.class, LlanowarElves.class, LightningBolt.class,
        JasperaSentinel.class, RavenousLindwurm.class, SculptorOfWinter.class,
        ValkiGodOfLies.class, TibaltCosmicImpostor.class})
class InSearchOfGreatnessTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a permanent whose mana value is one above another permanent")
    void castsMatchingPermanentFromHand() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        addCreatureReady(player1, new LlanowarElves());
        GrizzlyBears freeCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(freeCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Uses one when no other permanent is controlled")
    void excludesInSearchOfGreatnessFromManaValueCheck() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        LlanowarElves freeCard = new LlanowarElves();
        harness.setHand(player1, new ArrayList<>(List.of(freeCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
    }

    @Test
    @DisplayName("Scries when no eligible permanent is available")
    void scriesWhenNoPermanentMatches() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.setHand(player1, new ArrayList<>(List.of(new LightningBolt())));
        harness.setLibrary(player1, List.of(new LightningBolt()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scries after declining every eligible permanent")
    void scriesAfterDecliningAllOffers() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        addCreatureReady(player1, new LlanowarElves());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(first, second)));
        harness.setLibrary(player1, List.of(new LightningBolt()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void offersMatchingBackFaceOfModalDoubleFacedCard() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.setLibrary(player1, List.of(new JasperaSentinel()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    void determinesManaValueWhenTriggerResolves() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        var other = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        JasperaSentinel freeCard = new JasperaSentinel();
        harness.setHand(player1, List.of(freeCard));

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, other));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Jaspera Sentinel")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(freeCard);
    }

    @Test
    void acceptsSecondOfferAndCastsOnlyOneSpellWithoutScrying() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.addToBattlefield(player1, new JasperaSentinel());
        SculptorOfWinter first = new SculptorOfWinter();
        SculptorOfWinter second = new SculptorOfWinter();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new JasperaSentinel()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sculptor of Winter")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void usesGreatestOtherManaValueRatherThanAnyOtherManaValue() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new SculptorOfWinter());
        SculptorOfWinter wrongManaValue = new SculptorOfWinter();
        harness.setHand(player1, List.of(wrongManaValue));
        harness.setLibrary(player1, List.of(new JasperaSentinel()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wrongManaValue);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.setHand(player1, List.of(new JasperaSentinel()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
