package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The tap and untap triggers: {@link TapTrigger}. */
class TapTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final PermanentPredicate MERFOLK = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);
    private static final PermanentPredicate SOURCE = new PermanentIsSourceCardPredicate();

    @Test
    void everyTapTriggerFunctionsFromTheBattlefield() {
        assertThat(allFactories()).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD));
    }

    @Test
    void everyFactoryNamesItsOwnTrigger() {
        assertThat(allFactories()).doesNotHaveDuplicates();
    }

    @Test
    void namesTheEventThePermanentsRelationAndItsType() {
        assertThat(Triggers.tappedForMana())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.THIS));
        assertThat(Triggers.playerTapsLandForMana())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.ANY, Triggers.LAND))
                .isNotEqualTo(Triggers.playerTapsPermanentForMana());
        assertThat(Triggers.youTapArtifactTokenForMana().filter()).isEqualTo(Triggers.ARTIFACT_TOKEN);
        assertThat(Triggers.youTapCreatureForMana().relation()).isEqualTo(ObjectRelation.YOU_CONTROL);
        assertThat(Triggers.enchantedPermanentBecomesTapped().relation()).isEqualTo(ObjectRelation.ENCHANTED);
        assertThat(Triggers.permanentOpponentControlsBecomesTapped())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.OPPONENT_CONTROLS))
                .isNotEqualTo(Triggers.youTapPermanentOpponentControls());
        assertThat(Triggers.creatureBecomesTapped())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.ANY, Triggers.CREATURE));
        assertThat(Triggers.becomesUntapped())
                .isNotEqualTo(Triggers.permanentYouControlBecomesUntapped())
                .isNotEqualTo(Triggers.permanentBecomesUntapped());
        assertThat(Triggers.youUntapPermanentsDuringUntapStep().event())
                .isEqualTo(TapTrigger.TapEvent.YOU_UNTAP_DURING_UNTAP_STEP);
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new TapTrigger(null, ObjectRelation.THIS)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsTapAbilitiesByTrigger() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesUntapped(), DRAW);
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), GAIN);

        assertThat(card.getTriggerEffects(Triggers.becomesUntapped(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.permanentBecomesUntapped(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.becomesUntapped(), Zone.GRAVEYARD)).isEmpty();
    }

    @Test
    void aFilterIsDroppedFromTheWatchKey() {
        TapTrigger merfolk = Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK);

        assertThat(merfolk.qualifier()).isEqualTo(MERFOLK);
        assertThat(merfolk.watchKey()).isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.youTapCreatureForMana().watchKey()).isEqualTo(Triggers.youTapCreatureForMana());
        assertThatThrownBy(() -> merfolk.withFilter(MERFOLK)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.becomesTapped().withFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void thisPermanentAndTheFirstTimeDuringYourTurnAreWatchedByAPermanentYouControl() {
        assertThat(Triggers.becomesTapped().narrowed()).isTrue();
        assertThat(Triggers.becomesTapped().withFilter(MERFOLK).watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.becomesTappedForFirstTimeDuringYourTurn().watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn().watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.becomesUntapped().narrowed()).isFalse();
        assertThat(Triggers.becomesUntapped().watchKey()).isEqualTo(Triggers.becomesUntapped());
        assertThat(Triggers.permanentYouControlBecomesTapped().narrowed()).isFalse();
    }

    private static List<TapTrigger> allFactories() {
        return Stream.of(Triggers.tappedForMana(), Triggers.playerTapsLandForMana(),
                Triggers.opponentTapsLandForMana(), Triggers.playerTapsPermanentForMana(), Triggers.youTapCreatureForMana(),
                Triggers.youTapNonlandPermanentForMana(), Triggers.youTapArtifactTokenForMana(),
                Triggers.enchantedPermanentBecomesTapped(), Triggers.becomesTapped(),
                Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn(),
                Triggers.permanentYouControlBecomesTapped(),
                Triggers.permanentOpponentControlsBecomesTapped(), Triggers.creatureBecomesTapped(),
                Triggers.youTapPermanentOpponentControls(), Triggers.tappedToPayTeamworkCost(),
                Triggers.becomesUntapped(), Triggers.permanentYouControlBecomesUntapped(),
                Triggers.permanentBecomesUntapped(), Triggers.youUntapPermanentsDuringUntapStep()).toList();
    }
}
