package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JeskaiMonument;
import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Salt Road Skirmish")
@CardUsed({SaltRoadSkirmish.class, JeskaiMonument.class, KrumarInitiate.class})
class SaltRoadSkirmishTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates two hasty Warrior tokens")
    void destroysCreatureAndCreatesWarriors() {
        Permanent target = addCreature(player2);
        castSaltRoadSkirmish(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        assertThat(warriors).allSatisfy(warrior -> {
            assertThat(warrior.getCard().getPower()).isEqualTo(1);
            assertThat(warrior.getCard().getToughness()).isEqualTo(1);
            assertThat(warrior.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(warrior.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(warrior.hasKeyword(Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("Sacrifices the created Warriors at the next end step")
    void sacrificesCreatedWarriorsAtNextEndStep() {
        castSaltRoadSkirmish(addCreature(player2));
        harness.assertOnBattlefield(player1, "Warrior");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Warrior"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JeskaiMonument());
        harness.setHand(player1, List.of(new SaltRoadSkirmish()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castSaltRoadSkirmish(Permanent target) {
        harness.setHand(player1, List.of(new SaltRoadSkirmish()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Can destroy its controller's creature and still create Warriors")
    void canDestroyOwnCreature() {
        Permanent target = addCreature(player1);

        castSaltRoadSkirmish(target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Krumar Initiate");
        assertThat(findPermanents(player1, "Warrior")).hasSize(2);
    }

    @Test
    @DisplayName("Creates no Warriors if its only target leaves before resolution")
    void illegalTargetPreventsTokenCreation() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new SaltRoadSkirmish()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        harness.assertInGraveyard(player1, "Salt Road Skirmish");
    }

    @Test
    @DisplayName("Delayed sacrifice leaves other creatures alone and works with a missing Warrior")
    void sacrificesOnlyRemainingCreatedTokens() {
        Permanent otherCreature = addCreature(player1);
        castSaltRoadSkirmish(addCreature(player2));
        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(warriors.getFirst());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new KrumarInitiate());
    }
}
