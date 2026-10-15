package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MathiseSurgeChanneler.class, Divination.class, GrizzlyBears.class, LightningBolt.class,
        Cancel.class, HillGiant.class, Inspiration.class, Fireball.class})
class MathiseSurgeChannelerTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    @DisplayName("A result from 1 through 9 makes each player draw a card")
    void lowResultMakesEachPlayerDraw() {
        setRoll(9);
        castDivination(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A result from 10 through 19 makes the controller draw a card")
    void middleResultMakesControllerDraw() {
        setRoll(19);
        castDivination(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A result of 20 copies the triggering spell")
    void maximumResultCopiesTriggeringSpell() {
        setRoll(20);
        castDivination(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an instant with mana value less than 3")
    void doesNotTriggerForSmallInstant() {
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Includes an announced X value in the mana value threshold")
    void includesAnnouncedXValueInManaValueThreshold() {
        setRoll(9);
        Card xSpell = new Card();
        xSpell.setName("X Spell");
        xSpell.setType(CardType.SORCERY);
        xSpell.setManaCost("{X}");
        xSpell.addEffect(EffectSlot.SPELL, new DrawCardEffect(0));

        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(xSpell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void castDivination(List<Card> player1Library, List<Card> player2Library) {
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new Divination()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A natural twenty copies the spell even after it has been countered")
    void copiesCounteredSpell() {
        setRoll(20);
        Divination spell = new Divination();
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A qualifying instant can be copied with a different target")
    void copiesInstantWithNewTarget() {
        setRoll(20);
        prepareInspiration();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The copy keeps the original target when new targets are declined")
    void copiesInstantWithoutChangingTarget() {
        setRoll(20);
        prepareInspiration();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's qualifying instant does not trigger Mathise")
    void opponentSpellDoesNotTrigger() {
        setRoll(9);
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player2, List.of(new Inspiration()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Creature spells do not trigger Mathise even with mana value above three")
    void largeCreatureDoesNotTrigger() {
        setRoll(9);
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void prepareInspiration() {
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new Inspiration()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Fireball triggers only when its announced X brings mana value to at least three")
    void realXSpellRespectsManaValueThreshold(int x) {
        setRoll(9);
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new Fireball()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castSorcery(player1, 0, x, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20 - x);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(x == 2 ? 1 : 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(x == 2 ? 1 : 0);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
