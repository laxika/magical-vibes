package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootOut.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class})
class RootOutTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new RootOut()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Destroys target artifact and investigates")
    void destroysArtifactAndInvestigates() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        prepare();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys target enchantment and investigates")
    void destroysEnchantmentAndInvestigates() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelicChorus()).getId();
        prepare();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Can destroy its controller's artifact and the Clue draws on resolution")
    void destroysOwnArtifactAndCreatesUsableClue() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        harness.setLibrary(player1, List.of(new RootOut()));
        prepare();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Root Out");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when the targeted Clue is sacrificed in response")
    void doesNotInvestigateWhenOnlyTargetBecomesIllegal() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        harness.setLibrary(player1, List.of(new RootOut()));
        prepare();
        harness.castAndResolveSorcery(player1, 0, targetId);

        UUID clueId = findPermanent(player1, "Clue").getId();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));
        prepare();
        harness.castSorcery(player1, 0, clueId);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clue");
        harness.assertNotOnBattlefield(player2, "Clue");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Root Out"))).hasSize(2);
        harness.assertInHand(player1, "Root Out");
        assertThat(gd.stack).isEmpty();
    }
}
