package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherGust.class, CentaurCourser.class, FireElemental.class, Shock.class, AirElemental.class})
class AetherGustTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner may put a red permanent on top")
    void targetOwnerPutsRedPermanentOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        harness.setLibrary(player2, List.of(new CentaurCourser(), new Shock()));
        castAt(target);

        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly("Put it on top", "Put it on the bottom");

        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Fire Elemental");
        harness.assertNotOnBattlefield(player2, "Fire Elemental");
        harness.assertInGraveyard(player1, "Aether Gust");
    }

    @Test
    @DisplayName("The target's owner may put a green permanent on the bottom")
    void targetOwnerPutsGreenPermanentOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        harness.setLibrary(player2, List.of(new FireElemental(), new Shock()));
        castAt(target);

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Put it on the bottom");

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactly("Fire Elemental", "Shock", "Centaur Courser");
        harness.assertNotOnBattlefield(player2, "Centaur Courser");
    }

    @Test
    @DisplayName("Aether Gust can target a red spell")
    void targetsRedSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player2, List.of(new CentaurCourser()));

        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Centaur Courser", "Shock");
    }

    @Test
    @DisplayName("Aether Gust rejects a blue permanent")
    void rejectsBluePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsGreenCreatureSpellOnTopWithoutResolvingIt() {
        CentaurCourser courser = new CentaurCourser();
        harness.setHand(player1, List.of(courser, new AetherGust()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, courser.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put it on top");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(courser);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Centaur Courser", "Shock");
        harness.assertNotOnBattlefield(player1, "Centaur Courser");
        harness.assertNotInGraveyard(player1, "Centaur Courser");
    }

    @Test
    void ownerChoosesForPermanentControlledByOpponent() {
        FireElemental elemental = new FireElemental();
        elemental.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, elemental);
        harness.setLibrary(player2, List.of(new Shock()));
        castAt(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Shock", "Fire Elemental");
        harness.assertNotOnBattlefield(player1, "Fire Elemental");
    }

    @Test
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        harness.setLibrary(player2, List.of(new Shock()));
        castAt(target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).containsExactly("Shock");
        harness.assertInHand(player2, "Centaur Courser");
        harness.assertInGraveyard(player1, "Aether Gust");
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }

}
