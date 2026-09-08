package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThopterFabricatorTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a Thopter")
    void secondDrawCreatesThopter() {
        addFabricatorReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();

        draw(player1);
        resolveTopOfStack();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(1);
        Permanent thopter = thopters.getFirst();
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Drawing a third card in the same turn does not create another Thopter")
    void thirdDrawDoesNotCreateAnotherThopter() {
        addFabricatorReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 animates Thopter Fabricator and taps the crew")
    void crewAnimatesFabricator() {
        Permanent fabricator = addFabricatorReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        resolveTopOfStack();

        assertThat(gqs.isCreature(gd, fabricator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fabricator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fabricator)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addFabricatorReady(Player player) {
        Permanent permanent = new Permanent(new ThopterFabricator());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
