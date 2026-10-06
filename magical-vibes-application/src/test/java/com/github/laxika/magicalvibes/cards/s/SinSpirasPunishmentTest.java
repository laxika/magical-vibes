package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.ViviOrnitier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinSpirasPunishment.class, GrizzlyBears.class, Plains.class, Shock.class, ViviOrnitier.class})
class SinSpirasPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by casting and creates a tapped token copy of a random permanent")
    void entersAndCreatesTappedTokenCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveEnterTrigger(List.of(bears));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        List<Permanent> tokens = tokensNamed(player1, bears.getName());
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeats after exiling land cards")
    void repeatsAfterLandCards() {
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        resolveEnterTrigger(List.of(firstPlains, secondPlains));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstPlains, secondPlains);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList())
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Only permanent cards are eligible and the process stops after a nonland permanent")
    void onlyUsesPermanentCards() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        resolveEnterTrigger(List.of(bears, shock));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        assertThat(tokensNamed(player1, bears.getName())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers when Sin attacks")
    void triggersWhenAttacking() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent sin = harness.addToBattlefieldAndReturn(player1, new SinSpirasPunishment());
        sin.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        assertThat(tokensNamed(player1, bears.getName())).hasSize(1);
    }

    @Test
    void emptyGraveyardCreatesNoTokenAndDoesNotUseOpponentsGraveyard() {
        ViviOrnitier opponentCard = new ViviOrnitier();
        harness.setGraveyard(player2, List.of(opponentCard));

        resolveEnterTrigger(List.of());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void graveyardWithOnlyInstantsCreatesNoToken() {
        Shock shock = new Shock();

        resolveEnterTrigger(List.of(shock));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void stopsAfterOneNonlandEvenWhenMorePermanentsRemain() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();

        resolveEnterTrigger(List.of(first, second));

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(tokensNamed(player1, first.getName())).hasSize(1);
    }

    @Test
    void choosesFromGraveyardAtResolution() {
        GrizzlyBears removed = new GrizzlyBears();
        ViviOrnitier remaining = new ViviOrnitier();
        castSin(List.of(removed));
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(remaining));

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(remaining);
        assertThat(tokensNamed(player1, remaining.getName())).hasSize(1);
        assertThat(tokensNamed(player1, removed.getName())).isEmpty();
    }

    @Test
    void tokenCopyPreservesAllColorsOfMulticoloredCard() {
        ViviOrnitier vivi = new ViviOrnitier();

        resolveEnterTrigger(List.of(vivi));

        List<Permanent> tokens = tokensNamed(player1, vivi.getName());
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectiveColors(gd, tokens.getFirst()))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
    }

    private void resolveEnterTrigger(List<Card> graveyard) {
        castSin(graveyard);
        resolveAllTriggers();
    }

    private void castSin(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SinSpirasPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
    }

    private List<Permanent> tokensNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .toList();
    }
}
