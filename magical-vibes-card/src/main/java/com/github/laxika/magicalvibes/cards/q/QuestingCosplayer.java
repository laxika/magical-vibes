package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceDamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "345")
@CardRegistration(set = "MB2", collectorNumber = "583")
public class QuestingCosplayer extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("QuestingCosplayer", new OracleData(
                "Questing Cosplayer",
                CardType.CREATURE,
                Set.of(),
                "{1}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.HUMAN, CardSubtype.BARD),
                "When Questing Cosplayer enters the battlefield, create a Questing Role token and attach it "
                        + "to target creature. (If you control another Role on it, put that one into the graveyard. "
                        + "Enchanted creature has all the abilities of Questing Beast.)",
                1,
                1,
                Set.of(),
                null,
                null,
                null));
    }

    public QuestingCosplayer() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenAttachedToTargetEffect(questingRoleToken(), PlayerRelation.ANY));
    }

    private static CreateTokenEffect questingRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Questing Role",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new GrantKeywordEffect(
                                Set.of(Keyword.VIGILANCE, Keyword.DEATHTOUCH, Keyword.HASTE),
                                GrantScope.ENCHANTED_CREATURE),
                        new GrantEffectEffect(
                                new CantBeBlockedByCreaturesMatchingPredicateEffect(
                                        new PermanentPowerAtMostPredicate(2)),
                                GrantScope.ENCHANTED_CREATURE),
                        new GrantEffectEffect(
                                new SourceDamageCantBePreventedEffect(),
                                GrantScope.ENCHANTED_CREATURE),
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                        new EventValue(),
                                        new PermanentControlledByDefendingPlayerPredicate()),
                                GrantScope.ENCHANTED_CREATURE))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
