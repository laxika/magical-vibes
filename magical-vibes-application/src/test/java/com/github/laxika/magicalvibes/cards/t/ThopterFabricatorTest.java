package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThopterFabricator.class, GrizzlyBears.class})
class ThopterFabricatorTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a Thopter")
    void secondDrawCreatesThopter() {
        addCreatureReady(player1, new ThopterFabricator());
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
        addCreatureReady(player1, new ThopterFabricator());
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
        Permanent fabricator = addCreatureReady(player1, new ThopterFabricator());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        resolveTopOfStack();

        assertThat(gqs.isCreature(gd, fabricator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fabricator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fabricator)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void enteringAfterSecondDrawDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        draw(player1);
        harness.enterBattlefieldAndReturn(player1, new ThopterFabricator());

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("The first draw counts even when it precedes the Fabricator entering")
    void enteringAfterFirstDrawTriggersOnSecondDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        harness.enterBattlefieldAndReturn(player1, new ThopterFabricator());

        draw(player1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's second draw triggers during an opponent's turn")
    void secondDrawOnOpponentsTurnCreatesThopter() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new ThopterFabricator());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(findPermanents(player2, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Opponent draws do not trigger the Fabricator")
    void opponentDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new ThopterFabricator());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Each Fabricator triggers independently on the second draw")
    void multipleFabricatorsEachCreateToken() {
        harness.addToBattlefield(player1, new ThopterFabricator());
        harness.addToBattlefield(player1, new ThopterFabricator());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(2);
        resolveTopOfStack();
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
    }

    @Test
    @DisplayName("Summoning sick creatures can pay the crew cost")
    void summoningSickCreatureCanCrew() {
        Permanent fabricator = harness.addToBattlefieldAndReturn(player1, new ThopterFabricator());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveTopOfStack();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, fabricator)).isTrue();
        assertThat(gqs.hasKeyword(gd, fabricator, Keyword.FLYING)).isTrue();
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
