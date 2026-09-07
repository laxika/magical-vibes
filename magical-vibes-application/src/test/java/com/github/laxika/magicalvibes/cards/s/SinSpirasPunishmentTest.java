package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinSpirasPunishment.class, GrizzlyBears.class, Plains.class, Shock.class})
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
        Permanent sin = new Permanent(new SinSpirasPunishment());
        sin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sin);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        assertThat(tokensNamed(player1, bears.getName())).hasSize(1);
    }

    private void resolveEnterTrigger(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SinSpirasPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> tokensNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .toList();
    }
}
