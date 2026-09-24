package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalthorTheDefiled;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({FirecatBlitz.class, Mountain.class, BalthorTheDefiled.class})
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
    @DisplayName("Creates red 1/1 Elemental Cat creature tokens")
    void createsRedOneOneElementalCats() {
        harness.setHand(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        Permanent cat = findPermanents(player1, "Elemental Cat").getFirst();
        assertThat(cat.getCard().isToken()).isTrue();
        assertThat(cat.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(cat.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(cat.getCard().getSubtypes())
                .containsExactly(CardSubtype.ELEMENTAL, CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
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
    @DisplayName("Flashback sacrifices only the selected Mountains")
    void flashbackLeavesUnselectedMountainsOnBattlefield() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain3 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setGraveyard(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), null, null, List.of(), null, null,
                List.of(mountain1, mountain2), Map.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(mountain3);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental Cat")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Mountain")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Flashback rejects a non-Mountain sacrifice")
    void flashbackRejectsNonMountain() {
        UUID balthor = harness.addToBattlefieldAndReturn(player1, new BalthorTheDefiled()).getId();
        harness.setGraveyard(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 1, null, List.of(), null, null,
                List.of(), null, null, List.of(balthor), Map.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(balthor);
    }

    @Test
    @DisplayName("Flashback with X equal to zero needs no Mountains and creates no tokens")
    void flashbackWithZeroXNeedsNoMountains() {
        harness.setGraveyard(player1, List.of(new FirecatBlitz()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playFlashbackSpell(gd, player1, 0, 0, null, List.of(), null, null, List.of(), null, null,
                List.of(), Map.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental Cat")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
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
