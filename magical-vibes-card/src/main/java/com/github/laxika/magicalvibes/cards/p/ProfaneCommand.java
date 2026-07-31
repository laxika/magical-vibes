package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "135")
public class ProfaneCommand extends Card {

    public ProfaneCommand() {
        // Choose two — X is paid once and applies to every chosen mode. Modes 0/2 declare a
        // single target slot; mode 1 uses intrinsic graveyard targeting (targetId); mode 3 is
        // an X-scaled "up to X" creature group (must stay last in card-text order so flat
        // targetIds slicing stays unambiguous).
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player loses X life",
                        new LoseLifeEffect(new XValue(), LoseLifeRecipient.TARGET_PLAYER),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card with mana value X or less from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .targetGraveyard(true)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .requiresManaValueAtMostX(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -X/-X until end of turn",
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1)),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                ChooseOneEffect.ChooseOneOption.upToXTargets(
                        "Up to X target creatures gain fear until end of turn",
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Targets must be creatures"),
                        100)
        ), 2));
    }
}
