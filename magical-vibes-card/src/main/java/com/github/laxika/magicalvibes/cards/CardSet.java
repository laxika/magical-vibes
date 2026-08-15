package com.github.laxika.magicalvibes.cards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The card sets the game knows about, identified by their short set code.
 *
 * <p>Deliberately just an enum of codes. Which printings are implemented, the set's full name and
 * its card total all live on {@link CardCatalog}, whose implementation has a lifecycle and can be
 * injected — they used to be static maps here, which made every set's data JVM-wide mutable state
 * that tests had to scrub between cases.
 */
@RequiredArgsConstructor
public enum CardSet {

    SET_4ED("4ED"),
    SET_5ED("5ED"),
    SET_6ED("6ED"),
    SET_7ED("7ED"),
    SET_8ED("8ED"),
    SET_9ED("9ED"),
    SET_10E("10E"),
    SET_M10("M10"),
    SET_M11("M11"),
    SET_M12("M12"),
    SET_M13("M13"),
    SET_M14("M14"),
    SET_THS("THS"),
    SET_BNG("BNG"),
    SET_JOU("JOU"),
    SET_M15("M15"),
    SET_M19("M19"),
    SET_M20("M20"),
    SET_ORI("ORI"),
    SET_LRW("LRW"),
    SET_MOR("MOR"),
    SET_SHM("SHM"),
    SET_EVE("EVE"),
    SET_ECL("ECL"),
    SET_MRD("MRD"),
    SET_5DN("5DN"),
    SET_DST("DST"),
    SET_SOM("SOM"),
    SET_MBS("MBS"),
    SET_NPH("NPH"),
    SET_NEM("NEM"),
    SET_CHR("CHR"),
    SET_CHK("CHK"),
    SET_BOK("BOK"),
    SET_ISD("ISD"),
    SET_DKA("DKA"),
    SET_AVR("AVR"),
    SET_ZEN("ZEN"),
    SET_BFZ("BFZ"),
    SET_ZNR("ZNR"),
    SET_RTR("RTR"),
    SET_GTC("GTC"),
    SET_DGM("DGM"),
    SET_INR("INR"),
    SET_ICE("ICE"),
    SET_INV("INV"),
    SET_ALL("ALL"),
    SET_MIR("MIR"),
    SET_VIS("VIS"),
    SET_WTH("WTH"),
    SET_HML("HML"),
    SET_ALA("ALA"),
    SET_CON("CON"),
    SET_AKH("AKH"),
    SET_ARB("ARB"),
    SET_HOU("HOU"),
    SET_KTK("KTK"),
    SET_XLN("XLN"),
    SET_DOM("DOM"),
    SET_SOS("SOS"),
    SET_POR("POR"),
    SET_P02("P02"),
    SET_PTK("PTK"),
    SET_TMP("TMP"),
    SET_STH("STH"),
    SET_EXO("EXO"),
    SET_DRB("DRB"),
    SET_UDS("UDS"),
    SET_ODY("ODY"),
    SET_USG("USG"),
    SET_ITP("ITP"),
    SET_RQS("RQS"),
    SET_MGB("MGB"),
    SET_TPR("TPR"),
    SET_ATH("ATH"),
    SET_FDN("FDN"),
    SET_ULG("ULG");

    @Getter
    private final String code;

    /** The set with this code, or null when no set matches. */
    public static CardSet findByCode(String code) {
        for (CardSet set : values()) {
            if (set.code.equals(code)) {
                return set;
            }
        }
        return null;
    }
}
