package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.j.JeongJeongsDeserters;
import com.github.laxika.magicalvibes.cards.k.KnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthKingdomProtectors.class, JeongJeongsDeserters.class, KnowledgeSeeker.class, ArtificialEvolution.class, Lignify.class})
class EarthKingdomProtectorsTest extends BaseCardTest {

    @Test
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent protectors = addCreatureReady(player1, new EarthKingdomProtectors());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(protectors.isTapped()).isFalse();
        assertThat(protectors.isAttacking()).isTrue();
    }

    @Test
    void canSacrificeWhileTappedAndSummoningSick() {
        Permanent protectors = harness.addToBattlefieldAndReturn(player1, new EarthKingdomProtectors());
        protectors.setSummoningSick(true);
        protectors.tap();
        Permanent ally = addCreatureReady(player1, new JeongJeongsDeserters());

        harness.activateAbility(player1, 0, null, ally.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(protectors);
        harness.assertInGraveyard(player1, "Earth Kingdom Protectors");
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void abilityDoesNotResolveWhenTargetChangesController() {
        addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ally = addCreatureReady(player1, new JeongJeongsDeserters());

        harness.activateAbility(player1, 0, null, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerBattlefields.get(player2.getId()).add(ally);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Earth Kingdom Protectors");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Lignify.class})
    void canProtectNoncreatureKindredAlly() {
        addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent creature = addCreatureReady(player2, new KnowledgeSeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lignify());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handleListChoice(player1, "TREEFOLK");
        harness.handleListChoice(player1, "ALLY");
        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.ALLY)).isTrue();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aura, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Earth Kingdom Protectors");
    }

    @Test
    @DisplayName("Sacrificing grants indestructible to another Ally you control")
    void sacrificeGrantsIndestructibleToAnotherAlly() {
        Permanent protectors = addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ally = addCreatureReady(player1, new JeongJeongsDeserters());

        harness.activateAbility(player1, 0, null, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(protectors);
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ally = addCreatureReady(player1, new JeongJeongsDeserters());

        harness.activateAbility(player1, 0, null, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Only another Ally you control can be targeted")
    void restrictsTargetToAnotherControlledAlly() {
        Permanent protectors = addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ownAlly = addCreatureReady(player1, new JeongJeongsDeserters());
        Permanent ownNonAlly = addCreatureReady(player1, new KnowledgeSeeker());
        Permanent opponentAlly = addCreatureReady(player2, new JeongJeongsDeserters());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, protectors.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownNonAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
