package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirecatBlitz.class, Mountain.class})
class FirecatBlitzTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X hasty Elemental Cat tokens and exiles them at the next end step")
    void createsHastyCatsThatAreExiledAtNextEndStep() {
        harness.setHand(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Elemental Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allMatch(cat -> cat.getCard().isToken()
                && gqs.hasKeyword(gd, cat, Keyword.HASTE));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Elemental Cat")).isEmpty();
    }

    @Test
    @DisplayName("Casting Firecat Blitz for X=0 creates no tokens")
    void zeroXCreatesNoTokens() {
        harness.setHand(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental Cat")).isEmpty();
    }

    @Test
    @DisplayName("Flashback sacrifices X Mountains and creates the chosen number of tokens")
    void flashbackSacrificesMountainsForX() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setGraveyard(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);
        Map<UUID, Integer> noDamageAssignments = Map.of();

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), null, null, List.of(), null, null,
                List.of(mountain1, mountain2), noDamageAssignments);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental Cat")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Mountain"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Firecat Blitz"));
    }

    @Test
    @DisplayName("Flashback rejects fewer Mountains than the chosen X")
    void flashbackRequiresXMountains() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setGraveyard(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);
        Map<UUID, Integer> noDamageAssignments = Map.of();

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), null, null,
                List.of(), null, null, List.of(mountain), noDamageAssignments))
                .isInstanceOf(IllegalStateException.class);
    }
}
