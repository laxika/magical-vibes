package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "258")
public class HiveOfTheEyeTyrant extends Card {

    public HiveOfTheEyeTyrant() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCount(2, new PermanentIsLandPredicate()),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsCreature(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD),
                        GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new AnimatePermanentsEffect(
                        3, 3, List.of(CardSubtype.BEHOLDER), Set.of(Keyword.MENACE), CardColor.BLACK)),
                "{3}{B}: Until end of turn, this land becomes a 3/3 black Beholder creature with menace and "
                        + "\"Whenever this creature attacks, exile target card from defending player's graveyard.\" "
                        + "It's still a land."
        ));
    }
}
