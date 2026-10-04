package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "245")
@CardRegistration(set = "ORI", collectorNumber = "250")
@CardRegistration(set = "FDN", collectorNumber = "264")
@CardRegistration(set = "DDM", collectorNumber = "77")
@CardRegistration(set = "SLD", collectorNumber = "607")
@CardRegistration(set = "UMA", collectorNumber = "250")
@CardRegistration(set = "PIO", collectorNumber = "273")
@CardRegistration(set = "SOC", collectorNumber = "400")
@CardRegistration(set = "C15", collectorNumber = "302")
@CardRegistration(set = "CMM", collectorNumber = "426")
@CardRegistration(set = "WHO", collectorNumber = "299")
@CardRegistration(set = "WHO", collectorNumber = "890")
@CardRegistration(set = "PIP", collectorNumber = "283")
@CardRegistration(set = "PIP", collectorNumber = "811")
@CardRegistration(set = "C21", collectorNumber = "312")
@CardRegistration(set = "C20", collectorNumber = "303")
@CardRegistration(set = "LTC", collectorNumber = "326")
@CardRegistration(set = "MKC", collectorNumber = "284")
@CardRegistration(set = "MIC", collectorNumber = "179")
@CardRegistration(set = "C19", collectorNumber = "270")
@CardRegistration(set = "HOC", collectorNumber = "212")
@CardRegistration(set = "FIC", collectorNumber = "415")
public class RoguesPassage extends Card {

    public RoguesPassage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{4}, {T}: Target creature can't be blocked this turn.",
                TargetFilters.creature()
        ));
    }
}
