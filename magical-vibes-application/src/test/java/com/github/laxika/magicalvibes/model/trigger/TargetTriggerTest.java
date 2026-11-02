package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The becomes-the-target triggers: {@link TargetTrigger}. */
class TargetTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect SACRIFICE = new SacrificeSelfEffect();
    private static final CardEffect WARD = new CounterUnlessPaysEffect(2);

    @Test
    void functionsFromTheBattlefield() {
        assertThat(Triggers.becomesTargetOfOpponentSpellOrAbility().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youBecomeTargetOfSpell().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.playerChoosesTargets().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesWhatTargetsAndWhatBecomesTheTarget() {
        assertThat(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isEqualTo(new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                        ObjectRelation.YOU_CONTROL, Triggers.CREATURE))
                .isNotEqualTo(Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfOpponentSpell());
        assertThat(Triggers.becomesTargetOfOpponentSpellOrAbility())
                .isEqualTo(new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                        ObjectRelation.THIS))
                .isNotEqualTo(Triggers.becomesTargetOfOpponentSpell())
                .isNotEqualTo(Triggers.becomesTargetOfSpellOrAbility());
        assertThat(Triggers.becomesTargetOfSpell())
                .isNotEqualTo(Triggers.becomesTargetOfAuraSpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfSpell());
        assertThat(Triggers.creatureYouControlBecomesTargetOfSpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfBackupAbility())
                .isNotEqualTo(Triggers.creaturesYouControlBecomeTargetOfActivatedAbility());
        assertThat(Triggers.creatureBecomesTargetOfSpellOrAbility())
                .isNotEqualTo(Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility())
                .isNotEqualTo(Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility());
        assertThat(Triggers.youBecomeTargetOfSpell()).isNotEqualTo(Triggers.youBecomeTargetOfOpponentSpellOrAbility());
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new TargetTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsTargetAbilitiesByTrigger() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD);
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(), SACRIFICE);

        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD))
                .containsExactly(WARD);
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfSpellOrAbility(), Zone.BATTLEFIELD))
                .containsExactly(SACRIFICE);
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfOpponentSpellOrAbility(), Zone.GRAVEYARD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfSpell(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aPerpetualWardGrantIsReadByItsTrigger() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD));

        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.becomesTargetOfOpponentSpellOrAbility(),
                Zone.BATTLEFIELD)).containsExactly(WARD);
        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.becomesTargetOfOpponentSpell(),
                Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void thePerpetualKeywordGrantsTakeTheWardAbility() {
        TriggeredAbility ward = new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD);

        PerpetuallyGrantKeywordsToTargetCreatureEffect toTarget =
                new PerpetuallyGrantKeywordsToTargetCreatureEffect(Set.of(Keyword.WARD), ward);
        assertThat(toTarget.grantedAbility()).isEqualTo(ward);
        assertThat(toTarget.triggeredAbility()).isEqualTo(WARD);
        assertThat(toTarget.filter()).isInstanceOf(PermanentControlledBySourceControllerPredicate.class);

        PerpetuallyGrantKeywordToMatchingCardsEffect toMatching =
                new PerpetuallyGrantKeywordToMatchingCardsEffect(null, null, Set.of(Keyword.WARD), ward);
        assertThat(toMatching.grantedAbility()).isEqualTo(ward);
        assertThat(toMatching.graveyardFilter()).isNull();
        assertThat(toMatching.libraryFilter()).isNull();
    }
}
