package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WWK", collectorNumber = "132")
@CardRegistration(set = "ONC", collectorNumber = "145")
@CardRegistration(set = "SLD", collectorNumber = "1532")
@CardRegistration(set = "HA2", collectorNumber = "20")
@CardRegistration(set = "TSR", collectorNumber = "406")
@CardRegistration(set = "SOC", collectorNumber = "363")
@CardRegistration(set = "C13", collectorNumber = "278")
@CardRegistration(set = "CMD", collectorNumber = "267")
@CardRegistration(set = "C14", collectorNumber = "285")
@CardRegistration(set = "MB2", collectorNumber = "105")
@CardRegistration(set = "MOC", collectorNumber = "391")
@CardRegistration(set = "C21", collectorNumber = "281")
@CardRegistration(set = "DSC", collectorNumber = "265")
@CardRegistration(set = "LTC", collectorNumber = "358")
@CardRegistration(set = "LTC", collectorNumber = "388")
@CardRegistration(set = "TDC", collectorNumber = "341")
@CardRegistration(set = "MKC", collectorNumber = "250")
@CardRegistration(set = "AFC", collectorNumber = "226")
@CardRegistration(set = "OTC", collectorNumber = "273")
@CardRegistration(set = "LCC", collectorNumber = "320")
@CardRegistration(set = "C20", collectorNumber = "259")
@CardRegistration(set = "BLC", collectorNumber = "294")
@CardRegistration(set = "MIC", collectorNumber = "167")
@CardRegistration(set = "C19", collectorNumber = "232")
@CardRegistration(set = "BRC", collectorNumber = "38")
@CardRegistration(set = "BRC", collectorNumber = "176")
@CardRegistration(set = "WOC", collectorNumber = "152")
@CardRegistration(set = "C18", collectorNumber = "238")
@CardRegistration(set = "EOC", collectorNumber = "149")
@CardRegistration(set = "FDC", collectorNumber = "296")
public class BojukaBog extends Card {

    public BojukaBog() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
