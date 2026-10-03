package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Absorb;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.s.ScrabblingClaws;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BankruptInBlood.class, AxebaneBeast.class, Absorb.class, ScrabblingClaws.class})
class BankruptInBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two creatures as an additional cost and draws three cards")
    void sacrificesTwoCreaturesAndDrawsThreeCards() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());

        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Axebane Beast");
    }

    @Test
    @DisplayName("Cannot cast without sacrificing two creatures")
    void cannotCastWithoutTwoCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());

        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Axebane Beast");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast());

        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(ownCreature.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player1, "Axebane Beast");
        harness.assertOnBattlefield(player2, "Axebane Beast");
    }

    @Test
    @DisplayName("Tapped creatures are sacrificed during casting, before any cards are drawn")
    void sacrificesTappedCreaturesBeforeResolution() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        firstCreature.tap();
        secondCreature.tap();
        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
        harness.assertInGraveyard(player1, "Bankrupt in Blood");
    }

    @Test
    @DisplayName("Cannot sacrifice the same creature twice")
    void cannotSacrificeSameCreatureTwice() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), firstCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Bankrupt in Blood");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the additional cost by sacrificing three creatures")
    void cannotSacrificeThreeCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(firstCreature, secondCreature, thirdCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Bankrupt in Blood");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent for the additional cost")
    void cannotSacrificeNoncreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrabblingClaws());
        harness.setHand(player1, List.of(new BankruptInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Bankrupt in Blood");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering the spell does not refund the creatures and prevents the draw")
    void counteringDoesNotRefundSacrificesOrDrawCards() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        BankruptInBlood spell = new BankruptInBlood();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Bankrupt in Blood");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }
}
