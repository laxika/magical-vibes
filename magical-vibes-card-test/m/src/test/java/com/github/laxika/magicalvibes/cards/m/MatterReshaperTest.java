package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.UntamedHunger;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatterReshaper.class, WrathOfGod.class, Forest.class, SerraAngel.class,
        GraspOfDarkness.class, UntamedHunger.class})
class MatterReshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting Matter Reshaper's death trigger puts an eligible permanent onto the battlefield")
    void acceptsEligiblePermanent() {
        Forest forest = new Forest();
        setLibrary(forest);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Declining Matter Reshaper's death trigger puts an eligible permanent into hand")
    void declinesEligiblePermanent() {
        Forest forest = new Forest();
        setLibrary(forest);
        killMatterReshaper();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Matter Reshaper puts a revealed nonpermanent card into hand")
    void putsNonpermanentIntoHand() {
        WrathOfGod wrathOfGod = new WrathOfGod();
        setLibrary(wrathOfGod);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(wrathOfGod);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(wrathOfGod);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Matter Reshaper puts a revealed permanent with mana value greater than three into hand")
    void putsOverLimitPermanentIntoHand() {
        SerraAngel serraAngel = new SerraAngel();
        setLibrary(serraAngel);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(serraAngel);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(serraAngel);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    private void killMatterReshaper() {
        harness.addToBattlefield(player1, new MatterReshaper());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    @Test
    @DisplayName("A permanent with mana value exactly three can enter without being cast")
    void acceptsPermanentAtLimit() {
        MatterReshaper revealed = new MatterReshaper();
        harness.setLibrary(player1, List.of(revealed));
        killWithGraspOfDarkness();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(revealed));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
    }

    @Test
    @DisplayName("An empty library causes no choice and does not cause a failed draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        killWithGraspOfDarkness();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("A nonpermanent with mana value below three goes to hand without revealing further cards")
    void onlyTopCardMovesToHand() {
        GraspOfDarkness top = new GraspOfDarkness();
        MatterReshaper second = new MatterReshaper();
        harness.setLibrary(player1, List.of(top, second));
        killWithGraspOfDarkness();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A revealed Aura enters attached to a chosen legal creature")
    void choosesAttachmentForRevealedAura() {
        UntamedHunger aura = new UntamedHunger();
        harness.setLibrary(player1, List.of(aura));
        harness.addToBattlefield(player2, new MatterReshaper());
        var creatureId = harness.getPermanentId(player2, "Matter Reshaper");
        killWithGraspOfDarkness();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(aura);
                    assertThat(permanent.getAttachedTo()).isEqualTo(creatureId);
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Aura accepted with no legal creature to enchant remains on top of the library")
    void auraWithoutLegalAttachmentRemainsInLibrary() {
        UntamedHunger aura = new UntamedHunger();
        harness.setLibrary(player1, List.of(aura));
        killWithGraspOfDarkness();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura);
        harness.assertNotOnBattlefield(player1, "Untamed Hunger");
        harness.assertNotInGraveyard(player1, "Untamed Hunger");
    }

    private void killWithGraspOfDarkness() {
        harness.addToBattlefield(player1, new MatterReshaper());
        var creatureId = harness.getPermanentId(player1, "Matter Reshaper");
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creatureId);
    }
}
