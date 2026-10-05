package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GladeWatcher;
import com.github.laxika.magicalvibes.cards.s.SarkhansRage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjutaiExemplars.class, SarkhansRage.class, GladeWatcher.class, Forest.class})
class OjutaiExemplarsTest extends BaseCardTest {

    private static final String TAP_MODE = "Tap target creature";
    private static final String KEYWORD_MODE =
            "Ojutai Exemplars gains first strike and lifelink until end of turn";
    private static final String FLICKER_MODE =
            "Exile Ojutai Exemplars, then return it to the battlefield tapped under its owner's control";

    @Test
    @DisplayName("Tap mode taps a target creature")
    void tapMode() {
        addReadyExemplars();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeWatcher());

        castSarkhansRage();
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Keyword mode grants first strike and lifelink until end of turn")
    void keywordMode() {
        Permanent exemplars = addReadyExemplars();

        castSarkhansRage();
        harness.handleListChoice(player1, KEYWORD_MODE);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.LIFELINK)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Blink mode returns Ojutai Exemplars tapped under its owner's control")
    void blinkMode() {
        Permanent exemplars = addReadyExemplars();
        UUID oldId = exemplars.getId();

        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        assertThat(returned.getId()).isNotEqualTo(oldId);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Ojutai Exemplars")
    void creatureSpellDoesNotTrigger() {
        addReadyExemplars();
        harness.castFromHand(player1, new GladeWatcher(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        addReadyExemplars();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());

        castSarkhansRage();
        harness.handleListChoice(player1, TAP_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Ojutai Exemplars")
    void opponentsSpellDoesNotTrigger() {
        addReadyExemplars();
        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Blink mode returns a stolen Exemplars to its owner")
    void blinkReturnsToOwner() {
        Permanent exemplars = addReadyExemplars();
        gd.stolenCreatures.put(exemplars.getId(), player2.getId());

        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ojutai Exemplars")).isEmpty();
        Permanent returned = findPermanent(player2, "Ojutai Exemplars");
        assertThat(returned.getId()).isNotEqualTo(exemplars.getId());
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blink mode cannot return Exemplars killed in response")
    void blinkDoesNotReturnDeadSource() {
        Permanent exemplars = addReadyExemplars();
        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);

        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, exemplars.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ojutai Exemplars")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exemplars.getCard());
    }

    @Test
    @DisplayName("Blinking in response makes a removal spell lose its target")
    void blinkDodgesRemoval() {
        Permanent exemplars = addReadyExemplars();
        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castInstant(player2, 0, exemplars.getId());

        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ojutai Exemplars")).isSameAs(returned);
        assertThat(returned.getId()).isNotEqualTo(exemplars.getId());
        assertThat(returned.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A keyword trigger from before a blink cannot grant abilities to the returned creature")
    void oldKeywordTriggerDoesNotAffectReturnedCreature() {
        Permanent exemplars = addReadyExemplars();
        castSarkhansRage();
        harness.handleListChoice(player1, KEYWORD_MODE);

        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        assertThat(returned.getId()).isNotEqualTo(exemplars.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two stacked blink triggers only blink the original creature once")
    void oldBlinkTriggerDoesNotBlinkReturnedCreature() {
        addReadyExemplars();
        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);

        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ojutai Exemplars")).isSameAs(returned);
    }

    @Test
    @DisplayName("Blinking removes the keywords previously granted to Exemplars")
    void blinkRemovesGrantedKeywords() {
        Permanent exemplars = addReadyExemplars();
        castSarkhansRage();
        harness.handleListChoice(player1, KEYWORD_MODE);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.LIFELINK)).isTrue();

        castSarkhansRage();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A tap trigger resolves even if its source dies in response")
    void tapTriggerSurvivesSourceRemoval() {
        Permanent exemplars = addReadyExemplars();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeWatcher());
        castSarkhansRage();
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, exemplars.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Ojutai Exemplars")).isEmpty();
    }

    private Permanent addReadyExemplars() {
        return addCreatureReady(player1, new OjutaiExemplars());
    }

    private void castSarkhansRage() {
        harness.setHand(player1, List.of(new SarkhansRage()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, player2.getId());
    }
}
